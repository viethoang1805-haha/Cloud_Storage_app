package com.saas.cloud_storage_app.modules.user.mapper;

import com.saas.cloud_storage_app.common.utils.FileUtils;
import com.saas.cloud_storage_app.modules.user.dto.response.StorageResponse;
import com.saas.cloud_storage_app.modules.user.dto.response.UserResponse;
import com.saas.cloud_storage_app.modules.user.entity.Role;
import com.saas.cloud_storage_app.modules.user.entity.User;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

import static com.saas.cloud_storage_app.common.utils.FileUtils.formatSize;

@Component
public class UserMapper {

    // (1) User entity → UserResponse DTO
    public UserResponse toUserResponse(User user) {
        if (user == null) return null;

        // (1) Safe lấy roles — tránh NPE khi roles null
        List<String> roles = user.getRoles() == null
                ? List.of()
                : user.getRoles()
                .stream()
                .map(Role::getName)
                .filter(Objects::nonNull)
                .toList();

        return UserResponse.builder()
                .id(user.getId() != null ? user.getId().toString() : null)
                .email(user.getEmail())
                .fullName(user.getFullName())
                .avatarUrl(user.getAvatarUrl())
                .isEnabled(user.isEnabled())
                .roles(roles)
                .createdAt(user.getCreatedAt())
                .storage(buildStorageInfo(user))
                .build();
    }
    // (3) User entity → StorageResponse DTO
    private String formatSize(Long bytes) {
        if (bytes == null || bytes == 0) return "0 B";
        String[] units = {"B", "KB", "MB", "GB", "TB"};
        int idx = 0;
        double size = bytes;
        while (size >= 1024 && idx < units.length - 1) {
            size /= 1024;
            idx++;
        }
        return String.format("%.2f %s", size, units[idx]);
    }

    public StorageResponse toStorageResponse(User user) {
        Long used = user.getStorageUsed() != null ? user.getStorageUsed() : 0L;
        Long limit = user.getStorageLimit() != null
                ? user.getStorageLimit()
                : 5L * 1024 * 1024 * 1024;
        Long available = Math.max(0, limit - used);
        double percent = limit > 0 ? (double) used / limit * 100 : 0.0;

        return StorageResponse.builder()
                .usedBytes(used)
                .limitBytes(limit)
                .availableBytes(available)
                .usedFormatted(formatSize(used))
                .limitFormatted(formatSize(limit))
                .availableFormatted(formatSize(available))
                .usedPercent(Math.min(percent, 100.0))
                .build();
    }

    // (2) Tách method riêng để dễ debug
    private UserResponse.StorageInfo buildStorageInfo(User user) {
        Long used = user.getStorageUsed() != null ? user.getStorageUsed() : 0L;
        Long limit = user.getStorageLimit() != null
                ? user.getStorageLimit()
                : 5L * 1024 * 1024 * 1024; // 5GB default

        double percent = limit > 0
                ? (double) used / limit * 100
                : 0.0;

        return UserResponse.StorageInfo.builder()
                .usedBytes(used)
                .limitBytes(limit)
                .usedFormatted(formatSize(used))
                .limitFormatted(formatSize(limit))
                .usedPercent(Math.min(percent, 100.0))
                .build();
    }
}