package com.saas.cloud_storage_app.modules.user.entity;

import com.saas.cloud_storage_app.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseEntity {

    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "password", nullable = false, length = 255)
    private String password;  // lưu bcrypt hash, không bao giờ lưu plain text

    @Column(name = "full_name", nullable = false, length = 255)
    private String fullName;

    @Column(name = "avatar_url", length = 500)
    private String avatarUrl; // nullable — user chưa set avatar thì null

    @Column(name = "is_enabled", nullable = false)
    @Builder.Default
    private boolean isEnabled = true; // (1)

    @Column(name = "storage_used", nullable = false)
    @Builder.Default
    private Long storageUsed = 0L; // bytes đã dùng

    @Column(name = "storage_limit", nullable = false)
    @Builder.Default
    private Long storageLimit = 5368709120L; // 5GB mặc định

    // =============================================
    // (2) Quan hệ nhiều-nhiều với Role
    // =============================================
    @ManyToMany(fetch = FetchType.EAGER) // (3)
    @JoinTable(
            name = "user_roles",             // (4) tên bảng join
            joinColumns = @JoinColumn(name = "user_id"),           // (5)
            inverseJoinColumns = @JoinColumn(name = "role_id")     // (6)
    )
    @Builder.Default
    private Set<Role> roles = new HashSet<>();

    // =============================================
    // (7) Helper methods
    // =============================================
    public void addRole(Role role) {
        this.roles.add(role);
    }

    public void removeRole(Role role) {
        this.roles.remove(role);
    }

    public boolean hasStorageSpace(Long fileSize) {
        return (this.storageUsed + fileSize) <= this.storageLimit;
    }

    public void increaseStorageUsed(Long fileSize) {
        this.storageUsed += fileSize;
    }

    public void decreaseStorageUsed(Long fileSize) {
        this.storageUsed = Math.max(0, this.storageUsed - fileSize);
    }
}