package com.saas.cloud_storage_app.modules.notification.entity;

import com.saas.cloud_storage_app.common.entity.BaseEntity;
import com.saas.cloud_storage_app.modules.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification extends BaseEntity {

    // (1) Người nhận
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    private String message;

    // (2) Loại notification — dùng string thay vì enum
    // để dễ thêm loại mới không cần migration
    @Column(name = "type", nullable = false, length = 50)
    private String type;

    @Column(name = "is_read", nullable = false)
    @Builder.Default
    private boolean isRead = false;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    // (3) Reference đến object liên quan
    @Column(name = "ref_type", length = 50)
    private String refType;  // "FILE", "WORKSPACE", "MEMBER"

    @Column(name = "ref_id")
    private UUID refId;

    // =============================================
    // Helper methods
    // =============================================

    public void markAsRead() {
        this.isRead = true;
        this.readAt = LocalDateTime.now();
    }
}