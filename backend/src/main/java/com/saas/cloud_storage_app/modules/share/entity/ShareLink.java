package com.saas.cloud_storage_app.modules.share.entity;

import com.saas.cloud_storage_app.common.entity.BaseEntity;
import com.saas.cloud_storage_app.modules.file.entity.FileEntity;
import com.saas.cloud_storage_app.modules.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "share_links")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShareLink extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_id", nullable = false)
    private FileEntity file;

    // (1) Token unique dùng trong URL
    @Column(name = "token", nullable = false, unique = true, length = 100)
    private String token;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id", nullable = false)
    private User created;

    // (2) Password hash — null nếu không đặt password
    @Column(name = "password", length = 255)
    private String password;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean isActive = true;

    @Column(name = "download_count", nullable = false)
    @Builder.Default
    private int downloadCount = 0;

    @Column(name = "max_downloads")
    private Integer maxDownloads;

    // =============================================
    // Helper methods
    // =============================================

    // (3) Kiểm tra link còn hợp lệ không
    public boolean isValid() {
        if (!isActive) return false;

        // Kiểm tra hết hạn thời gian
        if (expiresAt != null && LocalDateTime.now().isAfter(expiresAt)) {
            return false;
        }

        // Kiểm tra hết lượt tải
        if (maxDownloads != null && downloadCount >= maxDownloads) {
            return false;
        }

        return true;
    }

    // (4) Kiểm tra link có bảo vệ bằng password không
    public boolean hasPassword() {
        return password != null && !password.isEmpty();
    }

    // (5) Tăng download count — gọi mỗi khi có người tải
    public void incrementDownloadCount() {
        this.downloadCount++;
    }
}