package com.saas.cloud_storage_app.modules.dashboard.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class SystemDashboardResponse {

    // (1) Số liệu toàn hệ thống
    private long totalUsers;
    private long totalWorkspaces;
    private long totalFiles;
    private long totalFolders;
    private Long totalStorageUsed;
    private String totalStorageFormatted;

    // (2) User đăng ký trong 30 ngày qua
    private long newUsersLast30Days;
    private long newFilesLast30Days;

    // (3) Top workspace theo storage
    private List<WorkspaceStorageStats> topWorkspacesByStorage;

    // (4) Hoạt động theo ngày trong 30 ngày qua
    private List<DailyStats> dailyStats;

    @Getter
    @Builder
    @AllArgsConstructor
    public static class WorkspaceStorageStats {
        private String workspaceId;
        private String workspaceName;
        private long fileCount;
        private Long storageUsed;
        private String storageFormatted;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class DailyStats {
        private String date;
        private long newUsers;
        private long newFiles;
        private long activeUsers;  // user có ít nhất 1 activity
    }
}