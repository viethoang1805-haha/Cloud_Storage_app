package com.saas.cloud_storage_app.modules.folder.entity;

import com.saas.cloud_storage_app.common.entity.BaseEntity;
import com.saas.cloud_storage_app.modules.user.entity.User;
import com.saas.cloud_storage_app.modules.workspace.entity.Workspace;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "folders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Folder extends BaseEntity {

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    // (1) Folder thuộc workspace nào
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workspace_id", nullable = false)
    private Workspace workspace;

    // (2) Self-referencing: folder cha của folder này
    // nullable vì folder gốc không có cha
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Folder parent;

    // (3) Danh sách folder con trực tiếp (chỉ 1 cấp, không đệ quy)
    @OneToMany(
            mappedBy = "parent",
            cascade = CascadeType.ALL,  // (4)
            orphanRemoval = true,       // (5)
            fetch = FetchType.LAZY
    )
    @Builder.Default
    private List<Folder> children = new ArrayList<>();

    // (6) User tạo folder
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id")
    private User createdByUser;

    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    private boolean isDeleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    // =============================================
    // (7) Helper methods
    // =============================================

    // Kiểm tra folder có phải folder gốc không
    public boolean isRootFolder() {
        return this.parent == null;
    }

    // Soft delete folder này (không xóa khỏi DB)
    public void softDelete() {
        this.isDeleted = true;
        this.deletedAt = LocalDateTime.now();
    }

    // Khôi phục folder đã xóa
    public void restore() {
        this.isDeleted = false;
        this.deletedAt = null;
    }

    // (8) Lấy đường dẫn đầy đủ — dùng để hiển thị breadcrumb
    // Ví dụ: "Documents / Reports / Q1-2024"
    public String getFullPath() {
        if (isRootFolder()) {
            return this.name;
        }
        return this.parent.getFullPath() + " / " + this.name;
        // (9) Chú ý: method này gọi đệ quy lên folder cha
        // Nếu folder lồng sâu và parent bị LAZY → có thể bị N+1
        // Giải pháp: dùng path materialization (xem phần 2.1 bên dưới)
    }
}