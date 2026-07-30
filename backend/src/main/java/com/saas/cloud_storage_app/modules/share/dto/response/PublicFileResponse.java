package com.saas.cloud_storage_app.modules.share.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class PublicFileResponse {

    private String token;
    private boolean hasPassword;
    private boolean passwordVerified; // (1) đã nhập đúng password chưa
    private LocalDateTime expiresAt;
    private int downloadCount;
    private Integer maxDownloads;

    // (2) Chỉ có khi passwordVerified = true hoặc không có password
    private FileDetail fileDetail;

    // (3) Download URL — chỉ có khi được phép
    private String downloadUrl;

    @Getter
    @Builder
    @AllArgsConstructor
    public static class FileDetail {
        private String originalName;
        private String contentType;
        private Long size;
        private String sizeFormatted;
        private boolean isImage;
    }
}