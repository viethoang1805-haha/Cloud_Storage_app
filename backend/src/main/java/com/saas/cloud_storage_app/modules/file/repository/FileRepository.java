package com.saas.cloud_storage_app.modules.file.repository;

import com.saas.cloud_storage_app.modules.file.entity.FileEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
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

    // (1) Đếm tất cả file chưa xóa
    @Query("SELECT COUNT(f) FROM FileEntity f WHERE f.isDeleted = false")
    long countAllNotDeleted();
    // (1) Đếm tổng file của user trong tất cả workspace
    @Query("""
    SELECT COUNT(f) FROM FileEntity f
    WHERE f.uploadedBy.id = :userId
    AND f.isDeleted = false
""")
    long countByUploadedByIdAndNotDeleted(@Param("userId") UUID userId);

    // (2) Đếm file trong workspace
    long countByWorkspaceIdAndIsDeletedFalse(UUID workspaceId);

    // (3) File gần đây nhất của user — dùng cho personal dashboard
    @Query("""
    SELECT f FROM FileEntity f
    WHERE f.uploadedBy.id = :userId
    AND f.isDeleted = false
    ORDER BY f.createdAt DESC
""")
    List<FileEntity> findRecentByUploadedById(
            @Param("userId") UUID userId,
            Pageable pageable
    );

    // (4) Phân tích file theo content type trong workspace
    @Query("""
    SELECT f.contentType, COUNT(f), SUM(f.size)
    FROM FileEntity f
    WHERE f.workspace.id = :workspaceId
    AND f.isDeleted = false
    GROUP BY f.contentType
""")
    List<Object[]> getFileTypeStats(@Param("workspaceId") UUID workspaceId);

    // (5) Top contributor trong workspace
    @Query("""
    SELECT f.uploadedBy.id,
           f.uploadedBy.fullName,
           f.uploadedBy.avatarUrl,
           COUNT(f),
           SUM(f.size)
    FROM FileEntity f
    WHERE f.workspace.id = :workspaceId
    AND f.isDeleted = false
    AND f.uploadedBy IS NOT NULL
    GROUP BY f.uploadedBy.id, f.uploadedBy.fullName, f.uploadedBy.avatarUrl
    ORDER BY COUNT(f) DESC
""")
    List<Object[]> getTopContributors(
            @Param("workspaceId") UUID workspaceId,
            Pageable pageable
    );

    // (6) Đếm file mới trong N ngày qua — toàn hệ thống
    @Query("""
    SELECT COUNT(f) FROM FileEntity f
    WHERE f.isDeleted = false
    AND f.createdAt >= :fromDate
""")
    long countFilesCreatedAfter(@Param("fromDate") LocalDateTime fromDate);
}