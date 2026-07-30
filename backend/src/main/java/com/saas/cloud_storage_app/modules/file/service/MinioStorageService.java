package com.saas.cloud_storage_app.modules.file.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

public interface MinioStorageService {

    // (1) Upload file lên MinIO, trả về storage key
    String uploadFile(MultipartFile file, String storageKey);

    // (2) Tạo presigned URL cho download (URL tạm thời)
    String generatePresignedDownloadUrl(String storageKey, int expiryMinutes);

    // (3) Tạo presigned URL cho upload trực tiếp từ browser (advanced)
    String generatePresignedUploadUrl(String storageKey, int expiryMinutes);

    // (4) Xóa file khỏi MinIO
    void deleteFile(String storageKey);

    // (5) Kiểm tra file có tồn tại trong MinIO không
    boolean fileExists(String storageKey);

    // (6) Lấy input stream của file — dùng khi cần serve file qua Spring
    InputStream getFileStream(String storageKey);
}