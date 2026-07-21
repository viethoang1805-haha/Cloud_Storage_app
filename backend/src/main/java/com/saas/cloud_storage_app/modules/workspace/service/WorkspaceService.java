package com.saas.cloud_storage_app.modules.workspace.service;

import com.saas.cloud_storage_app.modules.workspace.dto.request.WorkspaceCreateRequest;
import com.saas.cloud_storage_app.modules.workspace.dto.request.WorkspaceUpdateRequest;
import com.saas.cloud_storage_app.modules.workspace.dto.response.WorkspaceResponse;
import com.saas.cloud_storage_app.modules.workspace.entity.Workspace;

import java.util.List;
import java.util.UUID;

public interface WorkspaceService {
    WorkspaceResponse createWorkspace(String email, WorkspaceCreateRequest request);
    List<WorkspaceResponse> getMyWorkspaces(String email);
    WorkspaceResponse getWorkspaceById(String email, UUID workspaceId);
    WorkspaceResponse updateWorkspace(String email, UUID workspaceId, WorkspaceUpdateRequest request);
    void deleteWorkspace(String email, UUID workspaceId);

    // Internal — dùng cho module khác (file, folder)
    Workspace findWorkspaceById(UUID workspaceId);
    void validateMemberAccess(String email, UUID workspaceId);
}