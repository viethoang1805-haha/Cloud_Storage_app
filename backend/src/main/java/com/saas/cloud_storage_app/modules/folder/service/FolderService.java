package com.saas.cloud_storage_app.modules.folder.service;

import com.saas.cloud_storage_app.modules.folder.dto.request.FolderCreateRequest;
import com.saas.cloud_storage_app.modules.folder.dto.request.FolderMoveRequest;
import com.saas.cloud_storage_app.modules.folder.dto.request.FolderRenameRequest;
import com.saas.cloud_storage_app.modules.folder.dto.response.FolderResponse;
import com.saas.cloud_storage_app.modules.folder.dto.response.FolderTreeResponse;
import com.saas.cloud_storage_app.modules.folder.entity.Folder;

import java.util.List;
import java.util.UUID;

public interface FolderService {
    FolderResponse createFolder(String email, UUID workspaceId, FolderCreateRequest request);
    List<FolderResponse> getRootFolders(String email, UUID workspaceId);
    FolderResponse getFolderById(String email, UUID workspaceId, UUID folderId);
    FolderTreeResponse getFolderTree(String email, UUID workspaceId, UUID folderId);
    FolderResponse renameFolder(String email, UUID workspaceId, UUID folderId, FolderRenameRequest request);
    FolderResponse moveFolder(String email, UUID workspaceId, UUID folderId, FolderMoveRequest request);
    void deleteFolder(String email, UUID workspaceId, UUID folderId);
    List<FolderResponse> getChildFolders(String email, UUID workspaceId, UUID parentId);
    // Internal
    Folder findFolderById(UUID folderId);

}