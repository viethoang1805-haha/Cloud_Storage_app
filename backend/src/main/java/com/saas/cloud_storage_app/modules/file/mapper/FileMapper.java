package com.saas.cloud_storage_app.modules.file.mapper;

import com.saas.cloud_storage_app.common.utils.FileUtils;
import com.saas.cloud_storage_app.modules.file.dto.response.FilePageResponse;
import com.saas.cloud_storage_app.modules.file.dto.response.FileResponse;
import com.saas.cloud_storage_app.modules.file.entity.FileEntity;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class FileMapper {

    public FileResponse toFileResponse(FileEntity file) {
        return FileResponse.builder()
                .id(file.getId().toString())
                .originalName(file.getOriginalName())
                .contentType(file.getContentType())
                .size(file.getSize())
                .sizeFormatted(FileUtils.formatSize(file.getSize()))
                .extension(file.getExtension())
                .isImage(file.isImage())
                .isVideo(file.isVideo())
                .folder(file.getFolder() != null
                        ? FileResponse.FolderInfo.builder()
                        .id(file.getFolder().getId().toString())
                        .name(file.getFolder().getName())
                        .build()
                        : null
                )
                .workspaceId(file.getWorkspace().getId().toString())
                .uploadedBy(file.getUploadedBy() != null
                        ? FileResponse.UploaderInfo.builder()
                        .id(file.getUploadedBy().getId().toString())
                        .fullName(file.getUploadedBy().getFullName())
                        .avatarUrl(file.getUploadedBy().getAvatarUrl())
                        .build()
                        : null
                )
                .createdAt(file.getCreatedAt())
                .updatedAt(file.getUpdatedAt())
                .build();
    }

    // (1) Chuyển Page<FileEntity> → FilePageResponse
    public FilePageResponse toFilePageResponse(Page<FileEntity> page) {
        List<FileResponse> files = page.getContent()
                .stream()
                .map(this::toFileResponse)
                .toList();

        return FilePageResponse.builder()
                .files(files)
                .currentPage(page.getNumber())
                .totalPages(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .pageSize(page.getSize())
                .hasNext(page.hasNext())
                .hasPrevious(page.hasPrevious())
                .build();
    }
}