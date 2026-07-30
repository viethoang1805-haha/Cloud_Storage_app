package com.saas.cloud_storage_app.modules.user.service;

import com.saas.cloud_storage_app.modules.user.dto.request.ChangePasswordRequest;
import com.saas.cloud_storage_app.modules.user.dto.request.UpdateProfileRequest;
import com.saas.cloud_storage_app.modules.user.dto.response.StorageResponse;
import com.saas.cloud_storage_app.modules.user.dto.response.UserResponse;
import com.saas.cloud_storage_app.modules.user.entity.User;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface UserService {

    UserResponse getMyProfile(String email);

    UserResponse updateProfile(String email, UpdateProfileRequest request);

    void changePassword(String email, ChangePasswordRequest request);

    UserResponse uploadAvatar(String email, MultipartFile file);

    StorageResponse getStorageInfo(String email);

    // (1) Internal method — dùng cho các module khác (file, workspace...)
    User getUserByEmail(String email);

    User getUserById(java.util.UUID id);

    void increaseStorageUsed(UUID userId, Long size);
    void decreaseStorageUsed(UUID userId, Long size);
}