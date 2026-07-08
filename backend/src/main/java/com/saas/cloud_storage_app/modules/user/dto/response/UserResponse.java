package com.saas.cloud_storage_app.modules.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class UserResponse {

    private String id;
    private String email;
    private String fullName;
    private String avatarUrl;
    private boolean isEnabled;
    private List<String> roles;
    private LocalDateTime createdAt;

    // hiển thị dưới dạng dễ đọc
    private StorageInfo storage;

    @Getter
    @Builder
    @AllArgsConstructor
    public static class StorageInfo {
        private Long usedBytes;         // bytes thô — dùng để tính %
        private Long limitBytes;        // bytes thô
        private String usedFormatted;   // "2.5 GB" — hiển thị cho user
        private String limitFormatted;  // "5.0 GB"
        private Double usedPercent;     // 50.0 — dùng cho progress bar
    }
}