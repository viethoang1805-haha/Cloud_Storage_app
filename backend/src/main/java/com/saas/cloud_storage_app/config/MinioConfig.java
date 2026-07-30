package com.saas.cloud_storage_app.config;

import io.minio.MinioClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class MinioConfig {

    // (1) Đọc config từ application-dev.yml
    @Value("${minio.endpoint}")
    private String endpoint;

    @Value("${minio.access-key}")
    private String accessKey;

    @Value("${minio.secret-key}")
    private String secretKey;

    @Value("${minio.bucket-name}")
    private String bucketName;

    // (2) Tạo MinioClient bean — Spring inject vào bất kỳ đâu cần
    @Bean
    public MinioClient minioClient() {
        log.info("Khởi tạo MinIO client tại: {}", endpoint);

        return MinioClient.builder()
                .endpoint(endpoint)         // http://localhost:9000
                .credentials(accessKey, secretKey)
                .build();
    }
}