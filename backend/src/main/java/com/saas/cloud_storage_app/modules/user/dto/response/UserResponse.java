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

    // (1) Dùng @JsonProperty để đảm bảo serialize đúng
    @com.fasterxml.jackson.annotation.JsonProperty("isEnabled")
    private boolean isEnabled;

    private List<String> roles;
    private LocalDateTime createdAt;
    private StorageInfo storage;

    @Getter
    @Builder
    @AllArgsConstructor
    public static class StorageInfo {
        private Long usedBytes;
        private Long limitBytes;
        private String usedFormatted;
        private String limitFormatted;
        private Double usedPercent;
    }
}