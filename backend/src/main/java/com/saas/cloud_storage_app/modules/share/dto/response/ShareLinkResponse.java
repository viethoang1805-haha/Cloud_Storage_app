package com.saas.cloud_storage_app.modules.share.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class ShareLinkResponse {

    private String id;
    private String token;
    private String shareUrl;         // (1) full URL: "http://domain/share/token"
    private boolean hasPassword;     // (2) có password không (không trả password)
    private LocalDateTime expiresAt;
    private boolean isActive;
    private int downloadCount;
    private Integer maxDownloads;
    private LocalDateTime createdAt;

    // (3) Thông tin file được chia sẻ
    private FileInfo file;

    @Getter
    @Builder
    @AllArgsConstructor
    public static class FileInfo {
        private String id;
        private String originalName;
        private String contentType;
        private Long size;
        private String sizeFormatted;
    }
}