package com.saas.cloud_storage_app.modules.dashboard.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class PersonalDashboardResponse {

    // (1) Thông tin storage của user
    private StorageStats storage;

    // (2) Số liệu tổng quan
    private long totalFiles;
    private long totalFolders;
    private long totalWorkspaces;  // workspace user tham gia
    private long unreadNotifications;

    // (3) File được upload gần đây
    private List<RecentFileInfo> recentFiles;

    // (4) Workspace gần đây hoạt động
    private List<RecentWorkspaceInfo> recentWorkspaces;

    // =============================================
    // Inner classes
    // =============================================

    @Getter
    @Builder
    @AllArgsConstructor
    public static class StorageStats {
        private Long usedBytes;
        private Long limitBytes;
        private Long availableBytes;
        private String usedFormatted;
        private String limitFormatted;
        private String availableFormatted;
        private Double usedPercent;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class RecentFileInfo {
        private String id;
        private String originalName;
        private String contentType;
        private String sizeFormatted;
        private String workspaceName;
        private String folderName;    // null nếu ở root
        private java.time.LocalDateTime uploadedAt;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class RecentWorkspaceInfo {
        private String id;
        private String name;
        private String myRole;
        private long memberCount;
        private long fileCount;
    }
}