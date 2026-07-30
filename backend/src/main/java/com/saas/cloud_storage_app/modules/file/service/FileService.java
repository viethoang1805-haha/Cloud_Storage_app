package com.saas.cloud_storage_app.modules.file.service;

import com.saas.cloud_storage_app.modules.file.dto.request.FileSearchRequest;
import com.saas.cloud_storage_app.modules.file.dto.response.DownloadUrlResponse;
import com.saas.cloud_storage_app.modules.file.dto.response.FilePageResponse;
import com.saas.cloud_storage_app.modules.file.dto.response.FileResponse;
import com.saas.cloud_storage_app.modules.file.entity.FileEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface FileService {
    FileResponse uploadFile(String email, UUID workspaceId, UUID folderId, MultipartFile file);
    FilePageResponse getFilesInFolder(String email, UUID workspaceId, UUID folderId, int page, int size);
    FilePageResponse getFilesInRoot(String email, UUID workspaceId, int page, int size);
    FilePageResponse searchFiles(String email, UUID workspaceId, FileSearchRequest request);
    FileResponse getFileById(String email, UUID workspaceId, UUID fileId);
    DownloadUrlResponse getDownloadUrl(String email, UUID workspaceId, UUID fileId);
    FileResponse moveFile(String email, UUID workspaceId, UUID fileId, UUID targetFolderId);
    void deleteFile(String email, UUID workspaceId, UUID fileId);

    // Internal
    FileEntity findFileById(UUID fileId);
}