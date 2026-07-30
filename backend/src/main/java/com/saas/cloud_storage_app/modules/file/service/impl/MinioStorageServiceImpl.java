package com.saas.cloud_storage_app.modules.file.service.impl;

import com.saas.cloud_storage_app.common.exception.AppException;
import com.saas.cloud_storage_app.common.exception.ErrorCode;
import com.saas.cloud_storage_app.modules.file.service.MinioStorageService;
import io.minio.*;
import io.minio.errors.*;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class MinioStorageServiceImpl implements MinioStorageService {

    private final MinioClient minioClient;

    @Value("${minio.bucket-name}")
    private String bucketName;

    // =============================================
    // UPLOAD FILE
    // =============================================
    @Override
    public String uploadFile(MultipartFile file, String storageKey) {
        try {
            // (1) Đảm bảo bucket tồn tại, tạo nếu chưa có
            ensureBucketExists();

            // (2) Upload file lên MinIO
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(storageKey)           // đường dẫn trong bucket
                            .stream(
                                    file.getInputStream(),    // binary data
                                    file.getSize(),           // kích thước
                                    -1                        // (3) part size = -1 = auto
                            )
                            .contentType(file.getContentType()) // MIME type
                            .build()
            );

            log.info("Upload thành công: {}", storageKey);
            return storageKey;

        } catch (Exception e) {
            log.error("Upload thất bại cho key {}: {}", storageKey, e.getMessage());
            throw new AppException(ErrorCode.FILE_UPLOAD_FAILED);
        }
    }

    // =============================================
    // TẠO PRESIGNED URL DOWNLOAD
    // =============================================
    @Override
    public String generatePresignedDownloadUrl(String storageKey, int expiryMinutes) {
        try {
            // (4) Tạo URL có chữ ký tạm thời
            String url = minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)           // HTTP GET để download
                            .bucket(bucketName)
                            .object(storageKey)
                            .expiry(expiryMinutes, TimeUnit.MINUTES) // (5) URL hết hạn sau N phút
                            .build()
            );

            log.debug("Tạo presigned URL cho: {}", storageKey);
            return url;

        } catch (Exception e) {
            log.error("Không tạo được presigned URL: {}", e.getMessage());
            throw new AppException(ErrorCode.FILE_NOT_FOUND);
        }
    }

    // =============================================
    // TẠO PRESIGNED URL UPLOAD
    // =============================================
    @Override
    public String generatePresignedUploadUrl(String storageKey, int expiryMinutes) {
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.PUT)           // HTTP PUT để upload
                            .bucket(bucketName)
                            .object(storageKey)
                            .expiry(expiryMinutes, TimeUnit.MINUTES)
                            .build()
            );
        } catch (Exception e) {
            log.error("Không tạo được presigned upload URL: {}", e.getMessage());
            throw new AppException(ErrorCode.FILE_UPLOAD_FAILED);
        }
    }

    // =============================================
    // XÓA FILE
    // =============================================
    @Override
    public void deleteFile(String storageKey) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(storageKey)
                            .build()
            );
            log.info("Xóa file khỏi MinIO: {}", storageKey);

        } catch (Exception e) {
            // (6) Log lỗi nhưng không throw — xóa MinIO thất bại
            // không nên làm rollback cả transaction DB
            log.error("Xóa file MinIO thất bại {}: {}", storageKey, e.getMessage());
        }
    }

    // =============================================
    // KIỂM TRA FILE TỒN TẠI
    // =============================================
    @Override
    public boolean fileExists(String storageKey) {
        try {
            // (7) statObject throw exception nếu không tìm thấy
            minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucketName)
                            .object(storageKey)
                            .build()
            );
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // =============================================
    // LẤY INPUT STREAM
    // =============================================
    @Override
    public InputStream getFileStream(String storageKey) {
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(storageKey)
                            .build()
            );
        } catch (Exception e) {
            log.error("Lấy file stream thất bại: {}", e.getMessage());
            throw new AppException(ErrorCode.FILE_NOT_FOUND);
        }
    }

    // =============================================
    // PRIVATE HELPER
    // =============================================

    // (8) Tạo bucket nếu chưa tồn tại
    private void ensureBucketExists() {
        try {
            boolean exists = minioClient.bucketExists(
                    BucketExistsArgs.builder()
                            .bucket(bucketName)
                            .build()
            );

            if (!exists) {
                minioClient.makeBucket(
                        MakeBucketArgs.builder()
                                .bucket(bucketName)
                                .build()
                );
                log.info("Tạo bucket mới: {}", bucketName);
            }
        } catch (Exception e) {
            log.error("Lỗi kiểm tra/tạo bucket: {}", e.getMessage());
            throw new AppException(ErrorCode.FILE_UPLOAD_FAILED);
        }
    }
}