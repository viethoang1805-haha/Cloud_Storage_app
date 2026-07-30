package com.saas.cloud_storage_app.modules.share.service.impl;

import com.saas.cloud_storage_app.common.enums.PermissionType;
import com.saas.cloud_storage_app.common.exception.AppException;
import com.saas.cloud_storage_app.common.exception.ErrorCode;
import com.saas.cloud_storage_app.modules.file.entity.FileEntity;
import com.saas.cloud_storage_app.modules.file.service.FileService;
import com.saas.cloud_storage_app.modules.file.service.MinioStorageService;
import com.saas.cloud_storage_app.modules.share.dto.request.*;
import com.saas.cloud_storage_app.modules.share.dto.response.*;
import com.saas.cloud_storage_app.modules.share.entity.FilePermission;
import com.saas.cloud_storage_app.modules.share.entity.ShareLink;
import com.saas.cloud_storage_app.modules.share.mapper.ShareMapper;
import com.saas.cloud_storage_app.modules.share.repository.FilePermissionRepository;
import com.saas.cloud_storage_app.modules.share.repository.ShareLinkRepository;
import com.saas.cloud_storage_app.modules.share.service.ShareService;
import com.saas.cloud_storage_app.modules.user.entity.User;
import com.saas.cloud_storage_app.modules.user.service.UserService;
import com.saas.cloud_storage_app.modules.workspace.service.WorkspaceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShareServiceImpl implements ShareService {

    private final ShareLinkRepository shareLinkRepository;
    private final FilePermissionRepository filePermissionRepository;
    private final FileService fileService;
    private final MinioStorageService minioStorageService;
    private final WorkspaceService workspaceService;
    private final UserService userService;
    private final ShareMapper shareMapper;
    private final PasswordEncoder passwordEncoder;

    // (1) SecureRandom — cryptographically secure random
    // Không dùng Random thông thường vì có thể đoán được
    private final SecureRandom secureRandom = new SecureRandom();

    // =============================================
    // TẠO PUBLIC SHARE LINK
    // =============================================
    @Override
    @Transactional
    public ShareLinkResponse createShareLink(
            String email,
            UUID workspaceId,
            UUID fileId,
            CreateShareLinkRequest request) {

        User creator = userService.getUserByEmail(email);
        workspaceService.validateMemberAccess(email, workspaceId);
        FileEntity file = fileService.findFileById(fileId);

        // (2) Validate file thuộc workspace
        if (!file.getWorkspace().getId().equals(workspaceId)) {
            throw new AppException(ErrorCode.FILE_NOT_FOUND);
        }

        // (3) Nếu đã có link active thì trả về link cũ
        // (không tạo nhiều link cho 1 file — confuse user)
        if (shareLinkRepository.existsByFileIdAndIsActiveTrue(fileId)) {
            ShareLink existing = shareLinkRepository
                    .findByFileIdAndIsActiveTrue(fileId)
                    .orElseThrow();
            log.info("File {} đã có share link, trả về link cũ", fileId);
            return shareMapper.toShareLinkResponse(existing);
        }

        // (4) Tạo token ngẫu nhiên
        String token = generateSecureToken();

        // (5) Hash password nếu có
        String hashedPassword = null;
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            hashedPassword = passwordEncoder.encode(request.getPassword());
        }

        ShareLink shareLink = ShareLink.builder()
                .file(file)
                .token(token)
                .createdBy(creator)
                .password(hashedPassword)
                .expiresAt(request.getExpiresAt())
                .maxDownloads(request.getMaxDownloads())
                .build();

        ShareLink saved = shareLinkRepository.save(shareLink);

        log.info("Tạo share link cho file '{}' bởi: {}",
                file.getOriginalName(), email);

        return shareMapper.toShareLinkResponse(saved);
    }

    // =============================================
    // TRUY CẬP PUBLIC LINK
    // =============================================
    @Override
    @Transactional
    public PublicFileResponse accessShareLink(
            String token,
            AccessShareLinkRequest request) {

        // (6) Tìm link theo token
        ShareLink shareLink = shareLinkRepository.findByToken(token)
                .orElseThrow(() -> new AppException(ErrorCode.SHARE_LINK_NOT_FOUND));

        // (7) Kiểm tra link còn hợp lệ không
        if (!shareLink.isValid()) {
            throw new AppException(ErrorCode.SHARE_LINK_EXPIRED);
        }

        // (8) Kiểm tra password nếu link có password
        boolean passwordVerified = false;

        if (shareLink.hasPassword()) {
            String inputPassword = request != null ? request.getPassword() : null;

            if (inputPassword == null || inputPassword.isBlank()) {
                // Chưa nhập password → trả về info cơ bản, không có file detail
                return shareMapper.toPublicFileResponse(shareLink, false, null);
            }

            // (9) So sánh password bằng BCrypt
            if (!passwordEncoder.matches(inputPassword, shareLink.getPassword())) {
                throw new AppException(
                        ErrorCode.VALIDATION_FAILED,
                        "Mật khẩu không đúng"
                );
            }

            passwordVerified = true;
        } else {
            // Không có password → tự động verified
            passwordVerified = true;
        }

        // (10) Tăng download count sau khi xác thực thành công
        shareLinkRepository.incrementDownloadCount(shareLink.getId());

        // (11) Tạo presigned download URL
        String downloadUrl = minioStorageService.generatePresignedDownloadUrl(
                shareLink.getFile().getStorageKey(),
                30 // 30 phút cho public link (lâu hơn internal)
        );

        log.info("Truy cập share link token: {}", token);

        return shareMapper.toPublicFileResponse(shareLink, passwordVerified, downloadUrl);
    }

    // =============================================
    // LẤY SHARE LINK CỦA FILE
    // =============================================
    @Override
    @Transactional(readOnly = true)
    public ShareLinkResponse getShareLink(
            String email,
            UUID workspaceId,
            UUID fileId) {

        workspaceService.validateMemberAccess(email, workspaceId);

        ShareLink shareLink = shareLinkRepository
                .findByFileIdAndIsActiveTrue(fileId)
                .orElseThrow(() -> new AppException(ErrorCode.SHARE_LINK_NOT_FOUND));

        return shareMapper.toShareLinkResponse(shareLink);
    }

    // =============================================
    // VÔ HIỆU HÓA SHARE LINK
    // =============================================
    @Override
    @Transactional
    public void deactivateShareLink(
            String email,
            UUID workspaceId,
            UUID fileId) {

        User user = userService.getUserByEmail(email);
        workspaceService.validateMemberAccess(email, workspaceId);

        ShareLink shareLink = shareLinkRepository
                .findByFileIdAndIsActiveTrue(fileId)
                .orElseThrow(() -> new AppException(ErrorCode.SHARE_LINK_NOT_FOUND));

        // (12) Chỉ người tạo link hoặc ADMIN/OWNER workspace mới tắt được
        boolean isCreator = shareLink.getCreatedBy().getId().equals(user.getId());
        if (!isCreator) {
            workspaceService.validateAdminAccess(email, workspaceId);
        }

        shareLink.setActive(false);
        shareLinkRepository.save(shareLink);

        log.info("Vô hiệu hóa share link của file {} bởi: {}", fileId, email);
    }

    // =============================================
    // CHIA SẺ VỚI USER CỤ THỂ
    // =============================================
    @Override
    @Transactional
    public FilePermissionResponse shareWithUser(
            String email,
            UUID workspaceId,
            UUID fileId,
            ShareWithUserRequest request) {

        User sharer = userService.getUserByEmail(email);
        workspaceService.validateMemberAccess(email, workspaceId);
        FileEntity file = fileService.findFileById(fileId);

        if (!file.getWorkspace().getId().equals(workspaceId)) {
            throw new AppException(ErrorCode.FILE_NOT_FOUND);
        }

        // (13) Tìm user được chia sẻ
        User targetUser = userService.getUserByEmail(request.getEmail());

        // (14) Không tự chia sẻ với chính mình
        if (targetUser.getId().equals(sharer.getId())) {
            throw new AppException(
                    ErrorCode.VALIDATION_FAILED,
                    "Không thể chia sẻ file với chính mình"
            );
        }

        // (15) Nếu đã có permission → UPDATE thay vì INSERT
        if (filePermissionRepository.existsByFileIdAndUserId(fileId, targetUser.getId())) {
            filePermissionRepository.updatePermission(
                    fileId, targetUser.getId(), request.getPermission()
            );

            FilePermission updated = filePermissionRepository
                    .findByFileIdAndUserId(fileId, targetUser.getId())
                    .orElseThrow();

            log.info("Cập nhật permission {} cho user {} với file {}",
                    request.getPermission(), request.getEmail(), fileId);

            return shareMapper.toPermissionResponse(updated);
        }

        // (16) Tạo permission mới
        FilePermission permission = FilePermission.builder()
                .file(file)
                .user(targetUser)
                .sharedBy(sharer)
                .permission(request.getPermission())
                .expiresAt(request.getExpiresAt())
                .build();

        FilePermission saved = filePermissionRepository.save(permission);

        log.info("Chia sẻ file '{}' với {} quyền {} bởi: {}",
                file.getOriginalName(), request.getEmail(),
                request.getPermission(), email);

        return shareMapper.toPermissionResponse(saved);
    }

    // =============================================
    // DANH SÁCH PERMISSION CỦA FILE
    // =============================================
    @Override
    @Transactional(readOnly = true)
    public List<FilePermissionResponse> getFilePermissions(
            String email,
            UUID workspaceId,
            UUID fileId) {

        workspaceService.validateMemberAccess(email, workspaceId);

        return filePermissionRepository.findAllByFileId(fileId)
                .stream()
                .map(shareMapper::toPermissionResponse)
                .toList();
    }

    // =============================================
    // CẬP NHẬT PERMISSION
    // =============================================
    @Override
    @Transactional
    public FilePermissionResponse updatePermission(
            String email,
            UUID workspaceId,
            UUID fileId,
            UUID targetUserId,
            UpdatePermissionRequest request) {

        workspaceService.validateMemberAccess(email, workspaceId);

        // (17) Kiểm tra permission tồn tại
        FilePermission permission = filePermissionRepository
                .findByFileIdAndUserId(fileId, targetUserId)
                .orElseThrow(() -> new AppException(ErrorCode.MEMBER_NOT_FOUND));

        filePermissionRepository.updatePermission(
                fileId, targetUserId, request.getPermission()
        );

        // Reload
        FilePermission updated = filePermissionRepository
                .findByFileIdAndUserId(fileId, targetUserId)
                .orElseThrow();

        log.info("Cập nhật permission → {} cho userId {}",
                request.getPermission(), targetUserId);

        return shareMapper.toPermissionResponse(updated);
    }

    // =============================================
    // THU HỒI PERMISSION
    // =============================================
    @Override
    @Transactional
    public void revokePermission(
            String email,
            UUID workspaceId,
            UUID fileId,
            UUID targetUserId) {

        User user = userService.getUserByEmail(email);
        workspaceService.validateMemberAccess(email, workspaceId);

        FilePermission permission = filePermissionRepository
                .findByFileIdAndUserId(fileId, targetUserId)
                .orElseThrow(() -> new AppException(ErrorCode.MEMBER_NOT_FOUND));

        // (18) Chỉ người chia sẻ hoặc ADMIN mới thu hồi được
        boolean isSharer = permission.getSharedBy() != null
                && permission.getSharedBy().getId().equals(user.getId());

        if (!isSharer) {
            workspaceService.validateAdminAccess(email, workspaceId);
        }

        filePermissionRepository.deleteByFileIdAndUserId(fileId, targetUserId);

        log.info("Thu hồi permission của userId {} với file {} bởi: {}",
                targetUserId, fileId, email);
    }

    // =============================================
    // PRIVATE HELPERS
    // =============================================

    // (19) Tạo token ngẫu nhiên 12 ký tự
    private String generateSecureToken() {
        byte[] randomBytes = new byte[9]; // 9 bytes → 12 ký tự Base64
        secureRandom.nextBytes(randomBytes);
        // (20) URL-safe Base64: thay +/ bằng -_ để dùng được trong URL
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(randomBytes);
    }
}