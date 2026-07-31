package com.saas.cloud_storage_app.modules.activity.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ActivityLogResponse {

    private String id;

    // (1) Actor info — từ snapshot, luôn hiển thị đúng
    private ActorInfo actor;

    private String action;         // "FILE_UPLOADED"
    private String actionDisplay;  // (2) "Đã upload file" — human readable

    // (3) Workspace context
    private String workspaceId;
    private String workspaceName;

    // (4) Target object
    private String targetType;
    private UUID targetId;
    private String targetName;

    // (5) Metadata — flexible JSON
    private Map<String, Object> metadata;

    private String ipAddress;
    private LocalDateTime createdAt;

    @Getter
    @Builder
    @AllArgsConstructor
    public static class ActorInfo {
        private String email;
        private String name;
        // (6) Không có id — nếu user bị xóa, id là null
        // Nhưng email/name luôn có
    }
}