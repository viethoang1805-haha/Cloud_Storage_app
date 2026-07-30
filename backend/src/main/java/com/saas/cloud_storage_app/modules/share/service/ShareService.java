package com.saas.cloud_storage_app.modules.share.service;

import com.saas.cloud_storage_app.modules.share.dto.request.*;
import com.saas.cloud_storage_app.modules.share.dto.response.*;

import java.util.List;
import java.util.UUID;

public interface ShareService {

    // Share Link
    ShareLinkResponse createShareLink(
            String email, UUID workspaceId, UUID fileId,
            CreateShareLinkRequest request);

    PublicFileResponse accessShareLink(
            String token, AccessShareLinkRequest request);

    ShareLinkResponse getShareLink(
            String email, UUID workspaceId, UUID fileId);

    void deactivateShareLink(
            String email, UUID workspaceId, UUID fileId);

    // File Permission
    FilePermissionResponse shareWithUser(
            String email, UUID workspaceId, UUID fileId,
            ShareWithUserRequest request);

    List<FilePermissionResponse> getFilePermissions(
            String email, UUID workspaceId, UUID fileId);

    FilePermissionResponse updatePermission(
            String email, UUID workspaceId, UUID fileId,
            UUID targetUserId, UpdatePermissionRequest request);

    void revokePermission(
            String email, UUID workspaceId, UUID fileId,
            UUID targetUserId);
}