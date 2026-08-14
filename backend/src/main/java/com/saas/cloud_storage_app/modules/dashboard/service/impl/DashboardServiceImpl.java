package com.saas.cloud_storage_app.modules.dashboard.service.impl;

import com.saas.cloud_storage_app.common.utils.FileUtils;
import com.saas.cloud_storage_app.modules.activity.entity.ActivityLog;
import com.saas.cloud_storage_app.modules.activity.repository.ActivityLogRepository;
import com.saas.cloud_storage_app.modules.dashboard.dto.response.PersonalDashboardResponse;
import com.saas.cloud_storage_app.modules.dashboard.dto.response.SystemDashboardResponse;
import com.saas.cloud_storage_app.modules.dashboard.dto.response.WorkspaceDashboardResponse;
import com.saas.cloud_storage_app.modules.dashboard.service.DashboardService;
import com.saas.cloud_storage_app.modules.file.entity.FileEntity;
import com.saas.cloud_storage_app.modules.file.repository.FileRepository;
import com.saas.cloud_storage_app.modules.folder.repository.FolderRepository;
import com.saas.cloud_storage_app.modules.member.repository.WorkspaceMemberRepository;
import com.saas.cloud_storage_app.modules.notification.repository.NotificationRepository;
import com.saas.cloud_storage_app.modules.user.entity.User;
import com.saas.cloud_storage_app.modules.user.repository.UserRepository;
import com.saas.cloud_storage_app.modules.user.service.UserService;
import com.saas.cloud_storage_app.modules.workspace.entity.Workspace;
import com.saas.cloud_storage_app.modules.workspace.repository.WorkspaceRepository;
import com.saas.cloud_storage_app.modules.workspace.service.WorkspaceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final FileRepository fileRepository;
    private final FolderRepository folderRepository;
    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMemberRepository memberRepository;
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final ActivityLogRepository activityLogRepository;
    private final WorkspaceService workspaceService;
    private final UserService userService;

    // =============================================
    // PERSONAL DASHBOARD
    // =============================================
    @Override
    @Transactional(readOnly = true)
    public PersonalDashboardResponse getPersonalDashboard(String email) {
        User user = userService.getUserByEmail(email);

        // (1) Storage stats
        PersonalDashboardResponse.StorageStats storageStats = buildStorageStats(user);

        // (2) Tổng số file, folder, workspace
        long totalFiles = fileRepository
                .countByUploadedByIdAndNotDeleted(user.getId());

        long totalFolders = folderRepository
                .countByCreatedByUserIdAndNotDeleted(user.getId());

        // (3) Workspace user đang là member
        long totalWorkspaces = workspaceRepository
                .findAllByMemberId(user.getId()).size();

        // (4) Số thông báo chưa đọc
        long unreadNotifications = notificationRepository
                .countByUserIdAndIsReadFalse(user.getId());

        // (5) 10 file gần nhất
        List<FileEntity> recentFileEntities = fileRepository
                .findRecentByUploadedById(
                        user.getId(),
                        PageRequest.of(0, 10)
                );

        List<PersonalDashboardResponse.RecentFileInfo> recentFiles =
                recentFileEntities.stream()
                        .map(f -> PersonalDashboardResponse.RecentFileInfo.builder()
                                .id(f.getId().toString())
                                .originalName(f.getOriginalName())
                                .contentType(f.getContentType())
                                .sizeFormatted(FileUtils.formatSize(f.getSize()))
                                .workspaceName(f.getWorkspace().getName())
                                .folderName(f.getFolder() != null
                                        ? f.getFolder().getName() : null)
                                .uploadedAt(f.getCreatedAt())
                                .build()
                        )
                        .toList();

        // (6) Workspace gần đây
        List<Workspace> workspaces = workspaceRepository
                .findAllByMemberId(user.getId());

        List<PersonalDashboardResponse.RecentWorkspaceInfo> recentWorkspaces =
                workspaces.stream()
                        .limit(5)
                        .map(ws -> {
                            var member = memberRepository
                                    .findByWorkspaceIdAndUserId(
                                            ws.getId(), user.getId()
                                    ).orElseThrow();

                            long fileCount = fileRepository
                                    .countByWorkspaceIdAndIsDeletedFalse(ws.getId());

                            long memberCount = memberRepository
                                    .countByWorkspaceId(ws.getId());

                            return PersonalDashboardResponse.RecentWorkspaceInfo.builder()
                                    .id(ws.getId().toString())
                                    .name(ws.getName())
                                    .myRole(member.getRole().name())
                                    .memberCount(memberCount)
                                    .fileCount(fileCount)
                                    .build();
                        })
                        .toList();

        return PersonalDashboardResponse.builder()
                .storage(storageStats)
                .totalFiles(totalFiles)
                .totalFolders(totalFolders)
                .totalWorkspaces(totalWorkspaces)
                .unreadNotifications(unreadNotifications)
                .recentFiles(recentFiles)
                .recentWorkspaces(recentWorkspaces)
                .build();
    }

    // =============================================
    // WORKSPACE DASHBOARD
    // =============================================
    @Override
    @Transactional(readOnly = true)
    public WorkspaceDashboardResponse getWorkspaceDashboard(
            String email, UUID workspaceId) {

        workspaceService.validateMemberAccess(email, workspaceId);
        Workspace workspace = workspaceService.findWorkspaceById(workspaceId);

        // (7) Số liệu tổng quan
        long totalFiles = fileRepository
                .countByWorkspaceIdAndIsDeletedFalse(workspaceId);

        long totalFolders = folderRepository
                .countByWorkspaceIdAndIsDeletedFalse(workspaceId);

        long totalMembers = memberRepository
                .countByWorkspaceId(workspaceId);

        Long totalStorageUsed = fileRepository
                .getTotalSizeByWorkspaceId(workspaceId);

        // (8) File type stats
        List<Object[]> typeStatsRaw = fileRepository
                .getFileTypeStats(workspaceId);

        long totalFilesForPercent = typeStatsRaw.stream()
                .mapToLong(row -> (Long) row[1])
                .sum();

        List<WorkspaceDashboardResponse.FileTypeStats> fileTypeStats =
                typeStatsRaw.stream()
                        .map(row -> {
                            String contentType = (String) row[0];
                            long count = (Long) row[1];
                            Long size = (Long) row[2];

                            return WorkspaceDashboardResponse.FileTypeStats.builder()
                                    .type(categorizeContentType(contentType)) // (9)
                                    .count(count)
                                    .totalSize(size)
                                    .totalSizeFormatted(FileUtils.formatSize(size))
                                    .percentage(totalFilesForPercent > 0
                                            ? Math.round((double) count / totalFilesForPercent * 100 * 10.0) / 10.0
                                            : 0.0)
                                    .build();
                        })
                        // (10) Group lại theo category (nhiều contentType → cùng 1 category)
                        .collect(Collectors.groupingBy(
                                WorkspaceDashboardResponse.FileTypeStats::getType,
                                Collectors.collectingAndThen(
                                        Collectors.toList(),
                                        list -> WorkspaceDashboardResponse.FileTypeStats.builder()
                                                .type(list.get(0).getType())
                                                .count(list.stream().mapToLong(s -> s.getCount()).sum())
                                                .totalSize(list.stream().mapToLong(s ->
                                                        s.getTotalSize() != null ? s.getTotalSize() : 0L).sum())
                                                .totalSizeFormatted(FileUtils.formatSize(
                                                        list.stream().mapToLong(s ->
                                                                s.getTotalSize() != null ? s.getTotalSize() : 0L).sum()))
                                                .percentage(list.stream()
                                                        .mapToDouble(s -> s.getPercentage()).sum())
                                                .build()
                                )
                        ))
                        .values()
                        .stream()
                        .sorted(Comparator.comparingLong(
                                WorkspaceDashboardResponse.FileTypeStats::getCount).reversed())
                        .toList();

        // (11) Daily activity stats trong 7 ngày qua
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        List<WorkspaceDashboardResponse.DailyActivityStats> dailyActivities =
                buildDailyActivityStats(workspaceId, sevenDaysAgo);

        // (12) Top 5 contributors
        List<Object[]> contributorRaw = fileRepository
                .getTopContributors(workspaceId, PageRequest.of(0, 5));

        List<WorkspaceDashboardResponse.ContributorStats> topContributors =
                contributorRaw.stream()
                        .map(row -> {
                            Long size = row[4] != null ? (Long) row[4] : 0L;
                            return WorkspaceDashboardResponse.ContributorStats.builder()
                                    .userId(row[0].toString())
                                    .userName((String) row[1])
                                    .avatarUrl((String) row[2])
                                    .fileCount((Long) row[3])
                                    .totalSize(size)
                                    .totalSizeFormatted(FileUtils.formatSize(size))
                                    .build();
                        })
                        .toList();

        // (13) 10 hoạt động gần đây
        List<ActivityLog> recentLogs = activityLogRepository
                .findTop10ByWorkspaceIdOrderByCreatedAtDesc(workspaceId);

        List<WorkspaceDashboardResponse.RecentActivity> recentActivities =
                recentLogs.stream()
                        .map(log -> WorkspaceDashboardResponse.RecentActivity.builder()
                                .actorName(log.getActorName())
                                .actorEmail(log.getActorEmail())
                                .action(log.getAction())
                                .actionDisplay(mapActionDisplay(log.getAction()))
                                .targetName(log.getTargetName())
                                .createdAt(log.getCreatedAt())
                                .build()
                        )
                        .toList();

        return WorkspaceDashboardResponse.builder()
                .workspaceId(workspaceId.toString())
                .workspaceName(workspace.getName())
                .totalFiles(totalFiles)
                .totalFolders(totalFolders)
                .totalMembers(totalMembers)
                .totalStorageUsed(totalStorageUsed)
                .totalStorageFormatted(FileUtils.formatSize(totalStorageUsed))
                .fileTypeStats(fileTypeStats)
                .dailyActivities(dailyActivities)
                .topContributors(topContributors)
                .recentActivities(recentActivities)
                .build();
    }

    // =============================================
    // SYSTEM DASHBOARD — ADMIN ONLY
    // =============================================
    @Override
    @Transactional(readOnly = true)
    public SystemDashboardResponse getSystemDashboard() {
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);

        // (14) Số liệu toàn hệ thống
        long totalUsers = userRepository.count();
        long totalWorkspaces = workspaceRepository.count();
        long totalFiles = fileRepository.countAllNotDeleted();

        long totalFolders = folderRepository.count();

        // (15) Tổng storage — sum tất cả user
        Long totalStorageUsed = userRepository.findAll()
                .stream()
                .mapToLong(u -> u.getStorageUsed() != null ? u.getStorageUsed() : 0L)
                .sum();

        // (16) Số mới trong 30 ngày
        long newUsersLast30Days = userRepository
                .countUsersCreatedAfter(thirtyDaysAgo);

        long newFilesLast30Days = fileRepository
                .countFilesCreatedAfter(thirtyDaysAgo);

        // (17) Top 5 workspace theo storage
        List<Object[]> topWsRaw = workspaceRepository
                .findTopWorkspacesByStorage(PageRequest.of(0, 5));

        List<SystemDashboardResponse.WorkspaceStorageStats> topWorkspaces =
                topWsRaw.stream()
                        .map(row -> {
                            Long size = row[3] != null ? (Long) row[3] : 0L;
                            return SystemDashboardResponse.WorkspaceStorageStats.builder()
                                    .workspaceId(row[0].toString())
                                    .workspaceName((String) row[1])
                                    .fileCount((Long) row[2])
                                    .storageUsed(size)
                                    .storageFormatted(FileUtils.formatSize(size))
                                    .build();
                        })
                        .toList();

        // (18) Daily stats 30 ngày qua
        List<SystemDashboardResponse.DailyStats> dailyStats =
                buildSystemDailyStats(thirtyDaysAgo);

        return SystemDashboardResponse.builder()
                .totalUsers(totalUsers)
                .totalWorkspaces(totalWorkspaces)
                .totalFiles(totalFiles)
                .totalFolders(totalFolders)
                .totalStorageUsed(totalStorageUsed)
                .totalStorageFormatted(FileUtils.formatSize(totalStorageUsed))
                .newUsersLast30Days(newUsersLast30Days)
                .newFilesLast30Days(newFilesLast30Days)
                .topWorkspacesByStorage(topWorkspaces)
                .dailyStats(dailyStats)
                .build();
    }

    // =============================================
    // PRIVATE HELPERS
    // =============================================

    // (19) Build storage stats cho user
    private PersonalDashboardResponse.StorageStats buildStorageStats(User user) {
        Long used = user.getStorageUsed();
        Long limit = user.getStorageLimit();
        Long available = Math.max(0, limit - used);

        return PersonalDashboardResponse.StorageStats.builder()
                .usedBytes(used)
                .limitBytes(limit)
                .availableBytes(available)
                .usedFormatted(FileUtils.formatSize(used))
                .limitFormatted(FileUtils.formatSize(limit))
                .availableFormatted(FileUtils.formatSize(available))
                .usedPercent(FileUtils.calcUsedPercent(used, limit))
                .build();
    }

    // (20) Phân loại content type thành category đơn giản
    private String categorizeContentType(String contentType) {
        if (contentType == null) return "Other";

        if (contentType.startsWith("image/")) return "Image";
        if (contentType.startsWith("video/")) return "Video";
        if (contentType.startsWith("audio/")) return "Audio";
        if (contentType.startsWith("text/")) return "Text";

        return switch (contentType) {
            case "application/pdf" -> "PDF";
            case "application/msword",
                 "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
                    -> "Word";
            case "application/vnd.ms-excel",
                 "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                    -> "Excel";
            case "application/vnd.ms-powerpoint",
                 "application/vnd.openxmlformats-officedocument.presentationml.presentation"
                    -> "PowerPoint";
            case "application/zip",
                 "application/x-rar-compressed",
                 "application/x-7z-compressed"
                    -> "Archive";
            default -> "Other";
        };
    }

    // (21) Build daily activity stats
    private List<WorkspaceDashboardResponse.DailyActivityStats> buildDailyActivityStats(
            UUID workspaceId, LocalDateTime fromDate) {

        List<Object[]> rawData = activityLogRepository
                .getDailyActivityStats(workspaceId, fromDate);

        // (22) Group theo date
        Map<String, WorkspaceDashboardResponse.DailyActivityStats> statsMap =
                new LinkedHashMap<>();

        // Điền sẵn tất cả ngày trong 7 ngày với giá trị 0
        LocalDate current = fromDate.toLocalDate();
        LocalDate today = LocalDate.now();

        while (!current.isAfter(today)) {
            String dateStr = current.format(DateTimeFormatter.ISO_LOCAL_DATE);
            statsMap.put(dateStr,
                    WorkspaceDashboardResponse.DailyActivityStats.builder()
                            .date(dateStr)
                            .uploadCount(0)
                            .downloadCount(0)
                            .deleteCount(0)
                            .build()
            );
            current = current.plusDays(1);
        }

        // (23) Fill data từ query
        for (Object[] row : rawData) {
            String date = row[0].toString().substring(0, 10); // "2024-01-15"
            String action = (String) row[1];
            long count = (Long) row[2];

            var stats = statsMap.get(date);
            if (stats != null) {
                switch (action) {
                    case "FILE_UPLOADED" ->
                            stats = WorkspaceDashboardResponse.DailyActivityStats.builder()
                                    .date(stats.getDate())
                                    .uploadCount(count)
                                    .downloadCount(stats.getDownloadCount())
                                    .deleteCount(stats.getDeleteCount())
                                    .build();
                    case "FILE_DOWNLOADED" ->
                            stats = WorkspaceDashboardResponse.DailyActivityStats.builder()
                                    .date(stats.getDate())
                                    .uploadCount(stats.getUploadCount())
                                    .downloadCount(count)
                                    .deleteCount(stats.getDeleteCount())
                                    .build();
                    case "FILE_DELETED" ->
                            stats = WorkspaceDashboardResponse.DailyActivityStats.builder()
                                    .date(stats.getDate())
                                    .uploadCount(stats.getUploadCount())
                                    .downloadCount(stats.getDownloadCount())
                                    .deleteCount(count)
                                    .build();
                }
                statsMap.put(date, stats);
            }
        }

        return new ArrayList<>(statsMap.values());
    }

    // (24) Build system daily stats
    private List<SystemDashboardResponse.DailyStats> buildSystemDailyStats(
            LocalDateTime fromDate) {

        List<SystemDashboardResponse.DailyStats> result = new ArrayList<>();
        LocalDate current = fromDate.toLocalDate();
        LocalDate today = LocalDate.now();

        while (!current.isAfter(today)) {
            // (25) Đơn giản hóa: đếm user và file tạo trong ngày
            LocalDateTime dayStart = current.atStartOfDay();
            LocalDateTime dayEnd = current.plusDays(1).atStartOfDay();

            long newUsers = userRepository
                    .countUsersCreatedAfter(dayStart);
            long newFiles = fileRepository
                    .countFilesCreatedAfter(dayStart);

            result.add(SystemDashboardResponse.DailyStats.builder()
                    .date(current.format(DateTimeFormatter.ISO_LOCAL_DATE))
                    .newUsers(newUsers)
                    .newFiles(newFiles)
                    .activeUsers(0L) // TODO: cần thêm query
                    .build()
            );

            current = current.plusDays(1);
        }

        return result;
    }

    // (26) Map action → display text
    private String mapActionDisplay(String action) {
        return switch (action) {
            case "FILE_UPLOADED"      -> "Đã upload file";
            case "FILE_DOWNLOADED"    -> "Đã tải file";
            case "FILE_DELETED"       -> "Đã xóa file";
            case "FILE_SHARED"        -> "Đã chia sẻ file";
            case "FOLDER_CREATED"     -> "Đã tạo thư mục";
            case "FOLDER_DELETED"     -> "Đã xóa thư mục";
            case "MEMBER_INVITED"     -> "Đã mời thành viên";
            case "MEMBER_REMOVED"     -> "Đã xóa thành viên";
            case "WORKSPACE_CREATED"  -> "Đã tạo workspace";
            default                   -> action;
        };
    }
}