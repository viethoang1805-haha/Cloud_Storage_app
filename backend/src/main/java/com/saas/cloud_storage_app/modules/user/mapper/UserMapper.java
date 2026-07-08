package com.saas.cloud_storage_app.modules.user.mapper;

import com.saas.cloud_storage_app.common.utils.FileUtils;
import com.saas.cloud_storage_app.modules.user.dto.response.StorageResponse;
import com.saas.cloud_storage_app.modules.user.dto.response.UserResponse;
import com.saas.cloud_storage_app.modules.user.entity.User;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UserMapper {

    // (1) User entity → UserResponse DTO
    public UserResponse toUserResponse(User user) {
        List<String> roles = user.getRoles()
                .stream()
                .map(role -> role.getName())
                .toList();

        return UserResponse.builder()
                .id(user.getId().toString())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .avatarUrl(user.getAvatarUrl())
                .isEnabled(user.isEnabled())
                .roles(roles)
                .createdAt(user.getCreatedAt())
                .storage(buildStorageInfo(user))  // (2)
                .build();
    }

    // (3) User entity → StorageResponse DTO
    public StorageResponse toStorageResponse(User user) {
        Long used = user.getStorageUsed();
        Long limit = user.getStorageLimit();
        Long available = Math.max(0, limit - used);

        return StorageResponse.builder()
                .usedBytes(used)
                .limitBytes(limit)
                .availableBytes(available)
                .usedFormatted(FileUtils.formatSize(used))
                .limitFormatted(FileUtils.formatSize(limit))
                .availableFormatted(FileUtils.formatSize(available))
                .usedPercent(FileUtils.calcUsedPercent(used, limit))
                .build();
    }

    // (4) Helper private — build StorageInfo cho UserResponse
    private UserResponse.StorageInfo buildStorageInfo(User user) {
        Long used = user.getStorageUsed();
        Long limit = user.getStorageLimit();

        return UserResponse.StorageInfo.builder()
                .usedBytes(used)
                .limitBytes(limit)
                .usedFormatted(FileUtils.formatSize(used))
                .limitFormatted(FileUtils.formatSize(limit))
                .usedPercent(FileUtils.calcUsedPercent(used, limit))
                .build();
    }
}