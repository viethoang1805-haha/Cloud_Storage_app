package com.saas.cloud_storage_app.modules.member.entity;

import com.saas.cloud_storage_app.common.entity.BaseEntity;
import com.saas.cloud_storage_app.common.enums.WorkspaceRole;
import com.saas.cloud_storage_app.modules.user.entity.User;
import com.saas.cloud_storage_app.modules.workspace.entity.Workspace;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "workspace_members",
        uniqueConstraints = {
                // (1) Khai báo unique constraint ở đây để JPA validate
                // khớp với UNIQUE constraint trong SQL migration
                @UniqueConstraint(
                        name = "uq_workspace_member",
                        columnNames = {"workspace_id", "user_id"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkspaceMember extends BaseEntity {

    // (2) Quan hệ với Workspace
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workspace_id", nullable = false)
    private Workspace workspace;

    // (3) Quan hệ với User (thành viên)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // (4) Lưu enum dưới dạng String trong DB
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 50)
    @Builder.Default
    private WorkspaceRole role = WorkspaceRole.MEMBER;

    @Column(name = "joined_at", nullable = false)
    @Builder.Default
    private LocalDateTime joinedAt = LocalDateTime.now();

    // (5) Người mời — nullable vì owner không bị ai mời
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invited_by")
    private User invitedBy;

    // =============================================
    // (6) Helper methods kiểm tra quyền
    // =============================================

    public boolean isOwner() {
        return this.role == WorkspaceRole.OWNER;
    }

    public boolean isAdminOrAbove() {
        return this.role == WorkspaceRole.OWNER
                || this.role == WorkspaceRole.ADMIN;
    }

    public boolean canUpload() {
        // VIEWER không được upload
        return this.role != WorkspaceRole.VIEWER;
    }

    public boolean canDelete() {
        // Chỉ OWNER và ADMIN mới được xóa
        return isAdminOrAbove();
    }

    public boolean canManageMembers() {
        // Chỉ OWNER và ADMIN mới được mời/xóa thành viên
        return isAdminOrAbove();
    }
}