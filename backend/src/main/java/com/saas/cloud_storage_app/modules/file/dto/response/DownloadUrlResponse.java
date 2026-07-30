package com.saas.cloud_storage_app.modules.file.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class DownloadUrlResponse {

    private String fileId;
    private String originalName;
    private String downloadUrl;      // presigned URL từ MinIO
    private LocalDateTime expiresAt; // URL hết hạn sau bao lâu
    private Long size;
    private String contentType;
}