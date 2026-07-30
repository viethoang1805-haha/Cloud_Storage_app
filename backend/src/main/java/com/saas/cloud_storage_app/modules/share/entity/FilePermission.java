package com.saas.cloud_storage_app.modules.share.entity;

import com.saas.cloud_storage_app.common.entity.BaseEntity;
import com.saas.cloud_storage_app.common.enums.PermissionType;
import com.saas.cloud_storage_app.modules.file.entity.FileEntity;
import com.saas.cloud_storage_app.modules.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "file_permissions",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_file_permission",
                        columnNames = {"file_id", "user_id"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FilePermission extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_id", nullable = false)
    private FileEntity file;

    // (1) User được chia sẻ
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // (2) User chia sẻ
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shared_by_id", nullable = false)
    private User sharedBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "permission", nullable = false, length = 50)
    private PermissionType permission;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    // =============================================
    // Helper methods
    // =============================================

    // (3) Kiểm tra permission còn hiệu lực không
    public boolean isValid() {
        if (expiresAt != null && LocalDateTime.now().isAfter(expiresAt)) {
            return false;
        }
        return true;
    }

    // (4) Kiểm tra có quyền cụ thể không
    public boolean hasPermission(PermissionType required) {
        if (!isValid()) return false;

        // (5) Hierarchy: DELETE > EDIT > DOWNLOAD > VIEW
        // Ai có DELETE thì cũng có EDIT, DOWNLOAD, VIEW
        return switch (required) {
            case VIEW -> true; // mọi permission đều có VIEW
            case DOWNLOAD -> permission == PermissionType.DOWNLOAD
                    || permission == PermissionType.EDIT
                    || permission == PermissionType.DELETE;
            case EDIT -> permission == PermissionType.EDIT
                    || permission == PermissionType.DELETE;
            case DELETE -> permission == PermissionType.DELETE;
        };
    }
}