package com.saas.cloud_storage_app.modules.file.service.impl;

import com.saas.cloud_storage_app.common.exception.AppException;
import com.saas.cloud_storage_app.common.exception.ErrorCode;
import com.saas.cloud_storage_app.common.utils.FileUtils;
import com.saas.cloud_storage_app.modules.file.dto.request.FileSearchRequest;
import com.saas.cloud_storage_app.modules.file.dto.response.DownloadUrlResponse;
import com.saas.cloud_storage_app.modules.file.dto.response.FilePageResponse;
import com.saas.cloud_storage_app.modules.file.dto.response.FileResponse;
import com.saas.cloud_storage_app.modules.file.entity.FileEntity;
import com.saas.cloud_storage_app.modules.file.mapper.FileMapper;
import com.saas.cloud_storage_app.modules.file.repository.FileRepository;
import com.saas.cloud_storage_app.modules.file.service.FileService;
import com.saas.cloud_storage_app.modules.file.service.MinioStorageService;
import com.saas.cloud_storage_app.modules.folder.entity.Folder;
import com.saas.cloud_storage_app.modules.folder.service.FolderService;
import com.saas.cloud_storage_app.modules.member.repository.WorkspaceMemberRepository;
import com.saas.cloud_storage_app.modules.user.entity.User;
import com.saas.cloud_storage_app.modules.user.service.UserService;
import com.saas.cloud_storage_app.modules.workspace.entity.Workspace;
import com.saas.cloud_storage_app.modules.workspace.service.WorkspaceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    private final FileRepository fileRepository;
    private final MinioStorageService minioStorageService;
    private final WorkspaceService workspaceService;
    private final UserService userService;
    private final FolderService folderService;
    private final FileMapper fileMapper;

    private final ApplicationEventPublisher eventPublisher;  // (1) inject publisher
    private final WorkspaceMemberRepository memberRepository; // để lấy danh sách member


    // (1) Các loại file được phép upload
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            // Document
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "application/vnd.ms-powerpoint",
            "application/vnd.openxmlformats-officedocument.presentationml.presentation",
            "text/plain",
            "text/csv",
            // Image
            "image/jpeg",
            "image/png",
            "image/gif",
            "image/webp",
            "image/svg+xml",
            // Video
            "video/mp4",
            "video/mpeg",
            "video/quicktime",
            // Audio
            "audio/mpeg",
            "audio/wav",
            // Archive
            "application/zip",
            "application/x-rar-compressed",
            "application/x-7z-compressed"
    );

    // (2) Giới hạn kích thước file: 100MB
    private static final long MAX_FILE_SIZE = 100L * 1024 * 1024;

    // =============================================
    // UPLOAD FILE
    // =============================================
    @Override
    @Transactional
    public FileResponse uploadFile(
            String email,
            UUID workspaceId,
            UUID folderId,
            MultipartFile file) {

        // (3) Load các entity cần thiết
        User uploader = userService.getUserByEmail(email);
        Workspace workspace = workspaceService.findWorkspaceById(workspaceId);
        workspaceService.validateMemberAccess(email, workspaceId);

        // (4) Validate file
        validateFile(file);

        // (5) Kiểm tra quota storage của user
        if (!uploader.hasStorageSpace(file.getSize())) {
            throw new AppException(
                    ErrorCode.VALIDATION_FAILED,
                    String.format(
                            "Không đủ dung lượng. Còn trống: %s, File cần: %s",
                            FileUtils.formatSize(
                                    uploader.getStorageLimit() - uploader.getStorageUsed()
                            ),
                            FileUtils.formatSize(file.getSize())
                    )
            );
        }

        // (6) Xử lý folder đích
        Folder targetFolder = null;
        if (folderId != null) {
            targetFolder = folderService.findFolderById(folderId);
            // Đảm bảo folder thuộc đúng workspace
            if (!targetFolder.getWorkspace().getId().equals(workspaceId)) {
                throw new AppException(ErrorCode.FOLDER_NOT_FOUND);
            }
        }

        // (7) Tạo storage key unique
        String storageKey = FileUtils.generateStorageKey(
                uploader.getId().toString(),
                folderId != null ? folderId.toString() : "root",
                file.getOriginalFilename()
        );

        // (8) Upload lên MinIO TRƯỚC
        // Lý do: nếu DB save thành công nhưng MinIO thất bại
        // → user thấy file nhưng không download được
        // Ngược lại: MinIO thành công nhưng DB thất bại
        // → chỉ có orphan file trong MinIO (không hiển thị với user)
        // → ít nguy hại hơn
        minioStorageService.uploadFile(file, storageKey);

        // (9) Lưu metadata vào DB
        String extension = FileUtils.getExtension(
                file.getOriginalFilename() != null
                        ? file.getOriginalFilename()
                        : ""
        );

        FileEntity fileEntity = FileEntity.builder()
                .originalName(file.getOriginalFilename())
                .storageKey(storageKey)
                .contentType(file.getContentType())
                .size(file.getSize())
                .extension(extension)
                .workspace(workspace)
                .folder(targetFolder)
                .uploadedBy(uploader)
                .build();

        FileEntity saved = fileRepository.save(fileEntity);

        // (10) Cập nhật storage đã dùng của user
        userService.increaseStorageUsed(uploader.getId(), file.getSize());

        log.info("Upload thành công: {} ({}) bởi: {}",
                file.getOriginalFilename(),
                FileUtils.formatSize(file.getSize()),
                email
        );

        return fileMapper.toFileResponse(saved);
    }

    // =============================================
    // LẤY FILE TRONG FOLDER
    // =============================================
    @Override
    @Transactional(readOnly = true)
    public FilePageResponse getFilesInFolder(
            String email,
            UUID workspaceId,
            UUID folderId,
            int page,
            int size) {

        workspaceService.validateMemberAccess(email, workspaceId);

        // (11) Tạo Pageable với sort mặc định: mới nhất lên đầu
        Pageable pageable = PageRequest.of(
                page, size,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Page<FileEntity> filePage = fileRepository
                .findAllByFolderIdAndIsDeletedFalse(folderId, pageable);

        return fileMapper.toFilePageResponse(filePage);
    }

    // =============================================
    // LẤY FILE Ở ROOT WORKSPACE
    // =============================================
    @Override
    @Transactional(readOnly = true)
    public FilePageResponse getFilesInRoot(
            String email,
            UUID workspaceId,
            int page,
            int size) {

        workspaceService.validateMemberAccess(email, workspaceId);

        Pageable pageable = PageRequest.of(
                page, size,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Page<FileEntity> filePage = fileRepository
                .findAllByWorkspaceIdAndFolderIsNullAndIsDeletedFalse(
                        workspaceId, pageable
                );

        return fileMapper.toFilePageResponse(filePage);
    }

    // =============================================
    // TÌM KIẾM FILE
    // =============================================
    @Override
    @Transactional(readOnly = true)
    public FilePageResponse searchFiles(
            String email,
            UUID workspaceId,
            FileSearchRequest request) {

        workspaceService.validateMemberAccess(email, workspaceId);

        // (12) Build sort từ request
        Sort sort = buildSort(request.getSortBy(), request.getSortDir());
        Pageable pageable = PageRequest.of(
                request.getPage(),
                request.getSize(),
                sort
        );

        Page<FileEntity> filePage = fileRepository.searchByName(
                workspaceId,
                request.getKeyword() != null ? request.getKeyword() : "",
                pageable
        );

        return fileMapper.toFilePageResponse(filePage);
    }

    // =============================================
    // CHI TIẾT FILE
    // =============================================
    @Override
    @Transactional(readOnly = true)
    public FileResponse getFileById(
            String email,
            UUID workspaceId,
            UUID fileId) {

        workspaceService.validateMemberAccess(email, workspaceId);
        FileEntity file = findFileInWorkspace(fileId, workspaceId);

        return fileMapper.toFileResponse(file);
    }

    // =============================================
    // LẤY URL DOWNLOAD
    // =============================================
    @Override
    @Transactional(readOnly = true)
    public DownloadUrlResponse getDownloadUrl(
            String email,
            UUID workspaceId,
            UUID fileId) {

        workspaceService.validateMemberAccess(email, workspaceId);
        FileEntity file = findFileInWorkspace(fileId, workspaceId);

        // (13) Presigned URL hợp lệ trong 15 phút
        int expiryMinutes = 15;
        String downloadUrl = minioStorageService.generatePresignedDownloadUrl(
                file.getStorageKey(),
                expiryMinutes
        );

        return DownloadUrlResponse.builder()
                .fileId(file.getId().toString())
                .originalName(file.getOriginalName())
                .downloadUrl(downloadUrl)
                .expiresAt(LocalDateTime.now().plusMinutes(expiryMinutes))
                .size(file.getSize())
                .contentType(file.getContentType())
                .build();
    }

    // =============================================
    // DI CHUYỂN FILE
    // =============================================
    @Override
    @Transactional
    public FileResponse moveFile(
            String email,
            UUID workspaceId,
            UUID fileId,
            UUID targetFolderId) {

        workspaceService.validateMemberAccess(email, workspaceId);
        FileEntity file = findFileInWorkspace(fileId, workspaceId);

        // (14) targetFolderId = null → di chuyển về root
        Folder targetFolder = null;
        if (targetFolderId != null) {
            targetFolder = folderService.findFolderById(targetFolderId);
            if (!targetFolder.getWorkspace().getId().equals(workspaceId)) {
                throw new AppException(ErrorCode.FOLDER_NOT_FOUND);
            }
        }

        file.setFolder(targetFolder);
        FileEntity saved = fileRepository.save(file);

        log.info("Di chuyển file '{}' sang folder: {}",
                file.getOriginalName(),
                targetFolderId != null ? targetFolderId : "root"
        );

        return fileMapper.toFileResponse(saved);
    }

    // =============================================
    // XÓA FILE (SOFT DELETE)
    // =============================================
    @Override
    @Transactional
    public void deleteFile(
            String email,
            UUID workspaceId,
            UUID fileId) {

        User user = userService.getUserByEmail(email);
        FileEntity file = findFileInWorkspace(fileId, workspaceId);

        // (15) Chỉ người upload hoặc ADMIN/OWNER workspace mới xóa được
        boolean isUploader = file.getUploadedBy() != null
                && file.getUploadedBy().getId().equals(user.getId());

        if (!isUploader) {
            // Kiểm tra có phải ADMIN/OWNER không
            workspaceService.validateAdminAccess(email, workspaceId);
        }

        // (16) Soft delete trong DB
        fileRepository.softDeleteById(fileId);

        // (17) Giảm storage đã dùng
        userService.decreaseStorageUsed(user.getId(), file.getSize());

        // (18) Xóa thực tế trong MinIO (async hoặc sync tùy yêu cầu)
        // Với đồ án: xóa sync — đơn giản hơn
        // Production: nên dùng async job để không làm chậm response
        minioStorageService.deleteFile(file.getStorageKey());

        log.info("Soft delete file '{}' bởi: {}", file.getOriginalName(), email);
    }

    // =============================================
    // INTERNAL
    // =============================================
    @Override
    @Transactional(readOnly = true)
    public FileEntity findFileById(UUID fileId) {
        return fileRepository.findByIdAndIsDeletedFalse(fileId)
                .orElseThrow(() -> new AppException(ErrorCode.FILE_NOT_FOUND));
    }

    // =============================================
    // PRIVATE HELPERS
    // =============================================

    // (19) Validate file trước khi upload
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new AppException(
                    ErrorCode.VALIDATION_FAILED,
                    "File không được để trống"
            );
        }

        // Kiểm tra kích thước
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new AppException(
                    ErrorCode.VALIDATION_FAILED,
                    String.format(
                            "File quá lớn. Tối đa %s, file của bạn: %s",
                            FileUtils.formatSize(MAX_FILE_SIZE),
                            FileUtils.formatSize(file.getSize())
                    )
            );
        }

        // Kiểm tra MIME type
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new AppException(
                    ErrorCode.VALIDATION_FAILED,
                    "Loại file không được hỗ trợ: " + contentType
            );
        }
    }

    // (20) Tìm file và validate thuộc đúng workspace
    private FileEntity findFileInWorkspace(UUID fileId, UUID workspaceId) {
        FileEntity file = findFileById(fileId);
        if (!file.getWorkspace().getId().equals(workspaceId)) {
            throw new AppException(ErrorCode.FILE_NOT_FOUND);
        }
        return file;
    }

    // (21) Build Sort từ request params
    private Sort buildSort(String sortBy, String sortDir) {
        String field = switch (sortBy != null ? sortBy : "createdAt") {
            case "name" -> "originalName";
            case "size" -> "size";
            default -> "createdAt";
        };

        Sort.Direction direction = "asc".equalsIgnoreCase(sortDir)
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        return Sort.by(direction, field);
    }


}