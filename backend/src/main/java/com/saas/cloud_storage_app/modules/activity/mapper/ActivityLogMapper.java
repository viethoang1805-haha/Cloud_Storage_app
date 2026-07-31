package com.saas.cloud_storage_app.modules.activity.mapper;

import com.saas.cloud_storage_app.modules.activity.dto.response.ActivityLogPageResponse;
import com.saas.cloud_storage_app.modules.activity.dto.response.ActivityLogResponse;
import com.saas.cloud_storage_app.modules.activity.entity.ActivityLog;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class ActivityLogMapper {

    // (1) Map action code → display text tiếng Việt
    private static final Map<String, String> ACTION_DISPLAY = Map.ofEntries(
            Map.entry("USER_REGISTERED",    "Đã đăng ký tài khoản"),
            Map.entry("USER_LOGIN",         "Đã đăng nhập"),
            Map.entry("WORKSPACE_CREATED",  "Đã tạo workspace"),
            Map.entry("WORKSPACE_UPDATED",  "Đã cập nhật workspace"),
            Map.entry("MEMBER_INVITED",     "Đã mời thành viên"),
            Map.entry("MEMBER_REMOVED",     "Đã xóa thành viên"),
            Map.entry("FOLDER_CREATED",     "Đã tạo thư mục"),
            Map.entry("FOLDER_DELETED",     "Đã xóa thư mục"),
            Map.entry("FILE_UPLOADED",      "Đã upload file"),
            Map.entry("FILE_DOWNLOADED",    "Đã tải file"),
            Map.entry("FILE_DELETED",       "Đã xóa file"),
            Map.entry("FILE_SHARED",        "Đã chia sẻ file"),
            Map.entry("SHARE_LINK_CREATED", "Đã tạo link chia sẻ"),
            Map.entry("SHARE_LINK_REVOKED", "Đã thu hồi link chia sẻ")
    );

    public ActivityLogResponse toResponse(ActivityLog log) {
        return ActivityLogResponse.builder()
                .id(log.getId().toString())
                .actor(ActivityLogResponse.ActorInfo.builder()
                        .email(log.getActorEmail())
                        .name(log.getActorName())
                        .build()
                )
                .action(log.getAction())
                // (2) Lookup display text, fallback về action code nếu không có
                .actionDisplay(
                        ACTION_DISPLAY.getOrDefault(log.getAction(), log.getAction())
                )
                .workspaceId(log.getWorkspaceId() != null
                        ? log.getWorkspaceId().toString() : null)
                .workspaceName(log.getWorkspaceName())
                .targetType(log.getTargetType())
                .targetId(log.getTargetId())
                .targetName(log.getTargetName())
                .metadata(log.getMetadata())
                .ipAddress(log.getIpAddress())
                .createdAt(log.getCreatedAt())
                .build();
    }

    public ActivityLogPageResponse toPageResponse(Page<ActivityLog> page) {
        List<ActivityLogResponse> activities = page.getContent()
                .stream()
                .map(this::toResponse)
                .toList();

        return ActivityLogPageResponse.builder()
                .activities(activities)
                .currentPage(page.getNumber())
                .totalPages(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .hasNext(page.hasNext())
                .hasPrevious(page.hasPrevious())
                .build();
    }
}