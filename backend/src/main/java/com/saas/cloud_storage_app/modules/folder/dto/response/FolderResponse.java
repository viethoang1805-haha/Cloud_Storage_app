package com.saas.cloud_storage_app.modules.folder.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class FolderResponse {

    private String id;
    private String name;
    private String workspaceId;

    // (1) Thông tin folder cha — null nếu là folder gốc
    private ParentInfo parent;

    private long childCount;     // số folder con trực tiếp
    private boolean isDeleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // (2) Người tạo folder
    private CreatorInfo createdByUser;

    @Getter
    @Builder
    @AllArgsConstructor
    public static class ParentInfo {
        private String id;
        private String name;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class CreatorInfo {
        private String id;
        private String fullName;
        private String avatarUrl;
    }
}