package com.saas.cloud_storage_app.modules.file.entity;

import com.saas.cloud_storage_app.common.entity.BaseEntity;
import com.saas.cloud_storage_app.modules.folder.entity.Folder;
import com.saas.cloud_storage_app.modules.user.entity.User;
import com.saas.cloud_storage_app.modules.workspace.entity.Workspace;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "files")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileEntity extends BaseEntity {

    // (1) Tên gốc user upload
    @Column(name = "original_name", nullable = false, length = 255)
    private String originalName;

    // (2) Key trong MinIO — duy nhất, không đổi
    @Column(name = "storage_key", nullable = false, unique = true, length = 500)
    private String storageKey;

    // (3) MIME type
    @Column(name = "content_type", nullable = false, length = 100)
    private String contentType;

    // (4) Kích thước bytes
    @Column(name = "size", nullable = false)
    private Long size;

    // (5) Extension không có dấu chấm: "pdf", "png"
    @Column(name = "extension", length = 20)
    private String extension;

    // (6) Workspace chứa file
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workspace_id", nullable = false)
    private Workspace workspace;

    // (7) Folder chứa file — nullable
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "folder_id")
    private Folder folder;

    // (8) Người upload
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by_id")
    private User uploadedBy;

    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    private boolean isDeleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    // =============================================
    // (9) Helper methods
    // =============================================

    public void softDelete() {
        this.isDeleted = true;
        this.deletedAt = LocalDateTime.now();
    }

    // (10) Kiểm tra có phải ảnh không — dùng để generate thumbnail
    public boolean isImage() {
        return contentType != null && contentType.startsWith("image/");
    }

    // (11) Kiểm tra có phải video không
    public boolean isVideo() {
        return contentType != null && contentType.startsWith("video/");
    }
}