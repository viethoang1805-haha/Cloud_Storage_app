package com.saas.cloud_storage_app.modules.file.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class FileResponse {

    private String id;
    private String originalName;
    private String contentType;
    private Long size;
    private String sizeFormatted;    // "2.5 MB"
    private String extension;
    private boolean isImage;         // frontend dùng để hiển thị preview
    private boolean isVideo;

    // (1) Folder chứa file — null nếu ở root
    private FolderInfo folder;

    private String workspaceId;
    private UploaderInfo uploadedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // (2) URL download tạm thời — có thể null nếu không request
    // Sẽ được điền khi gọi API getDownloadUrl riêng
    private String downloadUrl;

    @Getter
    @Builder
    @AllArgsConstructor
    public static class FolderInfo {
        private String id;
        private String name;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class UploaderInfo {
        private String id;
        private String fullName;
        private String avatarUrl;
    }
}