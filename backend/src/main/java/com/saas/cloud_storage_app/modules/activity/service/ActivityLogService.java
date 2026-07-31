package com.saas.cloud_storage_app.modules.activity.service;

import com.saas.cloud_storage_app.modules.activity.dto.request.ActivityFilterRequest;
import com.saas.cloud_storage_app.modules.activity.dto.response.ActivityLogPageResponse;
import com.saas.cloud_storage_app.modules.activity.entity.ActivityLog;

import java.util.UUID;

public interface ActivityLogService {

    // (1) Lưu log — gọi từ các service khác
    void save(ActivityLog activityLog);

    // Query methods
    ActivityLogPageResponse getWorkspaceActivities(
            String email, UUID workspaceId, ActivityFilterRequest filter);

    ActivityLogPageResponse getMyActivities(
            String email, UUID workspaceId, ActivityFilterRequest filter);

    ActivityLogPageResponse getAllActivities(ActivityFilterRequest filter);
}