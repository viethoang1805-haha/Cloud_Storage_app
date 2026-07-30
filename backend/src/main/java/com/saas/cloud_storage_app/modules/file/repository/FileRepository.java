package com.saas.cloud_storage_app.modules.file.repository;

import com.saas.cloud_storage_app.modules.file.entity.FileEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FileRepository extends JpaRepository<FileEntity, UUID> {

    // (1) Tìm file theo id, chưa bị xóa
    Optional<FileEntity> findByIdAndIsDeletedFalse(UUID id);

    // (2) Tìm tất cả file trong 1 folder cụ thể
    Page<FileEntity> findAllByFolderIdAndIsDeletedFalse(
            UUID folderId, Pageable pageable
    );

    // (3) Tìm file ở root workspace (không thuộc folder nào)
    Page<FileEntity> findAllByWorkspaceIdAndFolderIsNullAndIsDeletedFalse(
            UUID workspaceId, Pageable pageable
    );

    // (4) Tìm tất cả file trong workspace (mọi folder)
    Page<FileEntity> findAllByWorkspaceIdAndIsDeletedFalse(
            UUID workspaceId, Pageable pageable
    );

    // (5) Tìm kiếm file theo tên — LIKE search đơn giản
    @Query("""
        SELECT f FROM FileEntity f
        WHERE f.workspace.id = :workspaceId
        AND f.isDeleted = false
        AND LOWER(f.originalName) LIKE LOWER(CONCAT('%', :keyword, '%'))
        ORDER BY f.createdAt DESC
    """)
    Page<FileEntity> searchByName(
            @Param("workspaceId") UUID workspaceId,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    // (6) Soft delete
    @Modifying
    @Query("""
        UPDATE FileEntity f
        SET f.isDeleted = true, f.deletedAt = CURRENT_TIMESTAMP
        WHERE f.id = :fileId
    """)
    void softDeleteById(@Param("fileId") UUID fileId);

    // (7) Tính tổng dung lượng file trong workspace — dùng cho dashboard
    @Query("""
        SELECT COALESCE(SUM(f.size), 0)
        FROM FileEntity f
        WHERE f.workspace.id = :workspaceId
        AND f.isDeleted = false
    """)
    Long getTotalSizeByWorkspaceId(@Param("workspaceId") UUID workspaceId);

    // (8) Đếm số file trong folder
    long countByFolderIdAndIsDeletedFalse(UUID folderId);

    // (9) Kiểm tra file thuộc workspace
    boolean existsByIdAndWorkspaceIdAndIsDeletedFalse(
            UUID fileId, UUID workspaceId
    );
}