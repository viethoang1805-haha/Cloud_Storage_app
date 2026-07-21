package com.saas.cloud_storage_app.modules.workspace.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class WorkspaceResponse {

    private String id;
    private String name;
    private String description;
    private boolean isPersonal;

    // (1) Thông tin owner — chỉ cần id và tên, không cần toàn bộ UserResponse
    private OwnerInfo owner;

    private long memberCount;     // (2) tổng số thành viên
    private String myRole;        // (3) role của user đang request trong workspace này
    private LocalDateTime createdAt;

    @Getter
    @Builder
    @AllArgsConstructor
    public static class OwnerInfo {
        private String id;
        private String fullName;
        private String email;
        private String avatarUrl;
    }
}