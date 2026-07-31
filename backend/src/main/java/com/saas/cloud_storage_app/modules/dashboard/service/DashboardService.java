package com.saas.cloud_storage_app.modules.dashboard.service;

import com.saas.cloud_storage_app.modules.dashboard.dto.response.PersonalDashboardResponse;
import com.saas.cloud_storage_app.modules.dashboard.dto.response.SystemDashboardResponse;
import com.saas.cloud_storage_app.modules.dashboard.dto.response.WorkspaceDashboardResponse;

import java.util.UUID;

public interface DashboardService {
    PersonalDashboardResponse getPersonalDashboard(String email);
    WorkspaceDashboardResponse getWorkspaceDashboard(String email, UUID workspaceId);
    SystemDashboardResponse getSystemDashboard();
}