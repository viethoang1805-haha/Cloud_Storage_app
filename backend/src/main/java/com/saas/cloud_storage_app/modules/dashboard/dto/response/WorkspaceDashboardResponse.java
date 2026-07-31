package com.saas.cloud_storage_app.modules.dashboard.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
@Builder
@AllArgsConstructor
public class WorkspaceDashboardResponse {

    // (1) Số liệu tổng quan workspace
    private String workspaceId;
    private String workspaceName;
    private long totalFiles;
    private long totalFolders;
    private long totalMembers;
    private Long totalStorageUsed;        // tổng bytes tất cả file
    private String totalStorageFormatted; // "45.2 MB"

    // (2) Phân tích file theo loại
    private List<FileTypeStats> fileTypeStats;

    // (3) Hoạt động theo ngày trong 7 ngày qua
    private List<DailyActivityStats> dailyActivities;

    // (4) Top contributors — ai upload nhiều nhất
    private List<ContributorStats> topContributors;

    // (5) Hoạt động gần đây
    private List<RecentActivity> recentActivities;

    // =============================================
    // Inner classes
    // =============================================

    @Getter
    @Builder
    @AllArgsConstructor
    public static class FileTypeStats {
        private String type;         // "PDF", "Image", "Video", "Other"
        private long count;
        private Long totalSize;
        private String totalSizeFormatted;
        private double percentage;   // % so với tổng
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class DailyActivityStats {
        private String date;         // "2024-01-15"
        private long uploadCount;
        private long downloadCount;
        private long deleteCount;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class ContributorStats {
        private String userId;
        private String userName;
        private String avatarUrl;
        private long fileCount;
        private Long totalSize;
        private String totalSizeFormatted;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class RecentActivity {
        private String actorName;
        private String actorEmail;
        private String action;
        private String actionDisplay;
        private String targetName;
        private java.time.LocalDateTime createdAt;
    }
}