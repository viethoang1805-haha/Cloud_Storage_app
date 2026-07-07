package com.saas.cloud_storage_app.modules.auth.entity;

import com.saas.cloud_storage_app.common.entity.BaseEntity;
import com.saas.cloud_storage_app.modules.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "refresh_tokens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken extends BaseEntity {

    // (1) Quan hệ nhiều-một: nhiều refresh token thuộc về 1 user
    // (user có thể đăng nhập nhiều thiết bị → nhiều refresh token)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "token", nullable = false, unique = true, length = 500)
    private String token;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "is_revoked", nullable = false)
    @Builder.Default
    private boolean isRevoked = false;

    // (2) Helper: kiểm tra token còn hạn không
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiresAt);
    }

    // (3) Helper: kiểm tra token có dùng được không
    public boolean isValid() {
        return !isRevoked && !isExpired();
    }
}