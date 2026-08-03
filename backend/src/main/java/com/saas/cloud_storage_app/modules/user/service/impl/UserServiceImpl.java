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

    // =============================================
    // XEM PROFILE
    // =============================================
    @Override
    @Transactional(readOnly = true)
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

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new AppException(ErrorCode.WRONG_PASSWORD);
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new AppException(ErrorCode.VALIDATION_FAILED,
                    "Mật khẩu mới và xác nhận mật khẩu không khớp");
        }

        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new AppException(ErrorCode.VALIDATION_FAILED,
                    "Mật khẩu mới không được trùng mật khẩu hiện tại");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        log.info("Đổi mật khẩu thành công: {}", email);
    }

    // =============================================
    // UPLOAD AVATAR
    // =============================================
    @Override
    @Transactional
    public UserResponse uploadAvatar(String email, MultipartFile file) {
        User user = getUserByEmail(email);
        validateAvatarFile(file);

        // TODO: Tích hợp MinIO ở bước sau
        String avatarUrl = "https://placeholder.com/avatar/" + user.getId();
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
    // INTERNAL METHODS
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

    @Override
    @Transactional
    public void increaseStorageUsed(UUID userId, Long size) {
        userRepository.increaseStorageUsed(userId, size);
    }

    @Override
    @Transactional
    public void decreaseStorageUsed(UUID userId, Long size) {
        userRepository.decreaseStorageUsed(userId, size);
    }

    // =============================================
    // PRIVATE HELPERS
    // =============================================
    private void validateAvatarFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new AppException(ErrorCode.VALIDATION_FAILED, "File ảnh không được để trống");
        }

        String extension = FileUtils.getExtension(
                file.getOriginalFilename() != null ? file.getOriginalFilename() : "");

        if (!java.util.Set.of("jpg", "jpeg", "png", "webp").contains(extension)) {
            throw new AppException(ErrorCode.VALIDATION_FAILED,
                    "Chỉ chấp nhận file ảnh định dạng JPG, PNG, WEBP");
        }

        if (file.getSize() > 5 * 1024 * 1024) {
            throw new AppException(ErrorCode.VALIDATION_FAILED,
                    "File ảnh không được vượt quá 5MB");
        }
    }
}