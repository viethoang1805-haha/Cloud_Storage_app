package com.saas.cloud_storage_app.modules.share.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class FilePermissionResponse {

    private String id;
    private String permission;       // "VIEW", "EDIT", "DOWNLOAD", "DELETE"
    private boolean isExpired;       // permission đã hết hạn chưa
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;

    private SharedUserInfo user;     // user được chia sẻ
    private SharedUserInfo sharedBy; // user chia sẻ

    @Getter
    @Builder
    @AllArgsConstructor
    public static class SharedUserInfo {
        private String id;
        private String fullName;
        private String email;
        private String avatarUrl;
    }
}