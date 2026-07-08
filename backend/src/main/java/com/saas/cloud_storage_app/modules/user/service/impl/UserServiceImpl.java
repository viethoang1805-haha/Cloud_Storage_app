package com.saas.cloud_storage_app.modules.user.service.impl;

import com.saas.cloud_storage_app.common.exception.AppException;
import com.saas.cloud_storage_app.common.exception.ErrorCode;
import com.saas.cloud_storage_app.common.utils.FileUtils;
import com.saas.cloud_storage_app.modules.user.dto.request.ChangePasswordRequest;
import com.saas.cloud_storage_app.modules.user.dto.request.UpdateProfileRequest;
import com.saas.cloud_storage_app.modules.user.dto.response.StorageResponse;
import com.saas.cloud_storage_app.modules.user.dto.response.UserResponse;
import com.saas.cloud_storage_app.modules.user.entity.User;
import com.saas.cloud_storage_app.modules.user.mapper.UserMapper;
import com.saas.cloud_storage_app.modules.user.repository.UserRepository;
import com.saas.cloud_storage_app.modules.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    // (1) MinIO service — sẽ implement ở Bước 9 (module file)
    // Tạm thời inject interface để compile được, implement sau
    // private final MinioStorageService minioStorageService;

    @Value("${minio.bucket-name}")
    private String bucketName;

    // =============================================
    // XEM PROFILE
    // =============================================
    @Override
    @Transactional(readOnly = true)  // (2)
    public UserResponse getMyProfile(String email) {
        User user = getUserByEmail(email);
        return userMapper.toUserResponse(user);
    }

    // =============================================
    // CẬP NHẬT PROFILE
    // =============================================
    @Override
    @Transactional
    public UserResponse updateProfile(String email, UpdateProfileRequest request) {
        User user = getUserByEmail(email);

        // (3) Chỉ cập nhật các field được phép thay đổi
        user.setFullName(request.getFullName().trim());

        User savedUser = userRepository.save(user);
        log.info("Cập nhật profile thành công: {}", email);

        return userMapper.toUserResponse(savedUser);
    }

    // =============================================
    // ĐỔI MẬT KHẨU
    // =============================================
    @Override
    @Transactional
    public void changePassword(String email, ChangePasswordRequest request) {
        User user = getUserByEmail(email);

        // (4) Bước 1: Kiểm tra mật khẩu hiện tại có đúng không
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new AppException(ErrorCode.WRONG_PASSWORD);
        }

        // (5) Bước 2: Kiểm tra mật khẩu mới và xác nhận có khớp không
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new AppException(
                    ErrorCode.VALIDATION_FAILED,
                    "Mật khẩu mới và xác nhận mật khẩu không khớp"
            );
        }

        // (6) Bước 3: Kiểm tra mật khẩu mới không trùng mật khẩu cũ
        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new AppException(
                    ErrorCode.VALIDATION_FAILED,
                    "Mật khẩu mới không được trùng mật khẩu hiện tại"
            );
        }

        // (7) Bước 4: Hash và lưu mật khẩu mới
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        log.info("Đổi mật khẩu thành công: {}", email);
        // (8) Lưu ý: Nên revoke tất cả refresh token sau đổi mật khẩu
        // Sẽ gọi RefreshTokenRepository.revokeAllByUserId ở đây
        // Tạm thời để trống, sẽ bổ sung sau khi có RefreshTokenRepository inject
    }

    // =============================================
    // UPLOAD AVATAR
    // =============================================
    @Override
    @Transactional
    public UserResponse uploadAvatar(String email, MultipartFile file) {
        User user = getUserByEmail(email);

        // (9) Validate file
        validateAvatarFile(file);

        // (10) TODO: Upload lên MinIO — sẽ implement ở Bước 9
        // String avatarKey = FileUtils.generateStorageKey(
        //         user.getId().toString(), "avatars", file.getOriginalFilename()
        // );
        // String avatarUrl = minioStorageService.uploadFile(file, avatarKey);

        // Tạm thời dùng placeholder
        String avatarUrl = "https://placeholder.com/avatar/" + user.getId();

        // (11) Xóa avatar cũ nếu có
        // if (user.getAvatarUrl() != null) {
        //     minioStorageService.deleteFile(extractKeyFromUrl(user.getAvatarUrl()));
        // }

        user.setAvatarUrl(avatarUrl);
        User savedUser = userRepository.save(user);

        log.info("Upload avatar thành công: {}", email);
        return userMapper.toUserResponse(savedUser);
    }

    // =============================================
    // XEM DUNG LƯỢNG
    // =============================================
    @Override
    @Transactional(readOnly = true)
    public StorageResponse getStorageInfo(String email) {
        User user = getUserByEmail(email);
        return userMapper.toStorageResponse(user);
    }

    // =============================================
    // INTERNAL METHODS — dùng cho các module khác
    // =============================================
    @Override
    @Transactional(readOnly = true)
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }

    @Override
    @Transactional(readOnly = true)
    public User getUserById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }

    // =============================================
    // PRIVATE HELPERS
    // =============================================

    // (12) Validate file avatar
    private void validateAvatarFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new AppException(
                    ErrorCode.VALIDATION_FAILED,
                    "File ảnh không được để trống"
            );
        }

        // (13) Kiểm tra định dạng file
        String extension = FileUtils.getExtension(
                file.getOriginalFilename() != null
                        ? file.getOriginalFilename()
                        : ""
        );

        if (!java.util.Set.of("jpg", "jpeg", "png", "webp").contains(extension)) {
            throw new AppException(
                    ErrorCode.VALIDATION_FAILED,
                    "Chỉ chấp nhận file ảnh định dạng JPG, PNG, WEBP"
            );
        }

        // (14) Kiểm tra kích thước file (tối đa 5MB)
        long maxSize = 5 * 1024 * 1024; // 5MB
        if (file.getSize() > maxSize) {
            throw new AppException(
                    ErrorCode.VALIDATION_FAILED,
                    "File ảnh không được vượt quá 5MB"
            );
        }
    }
}