package com.saas.cloud_storage_app.modules.folder.repository;

import com.saas.cloud_storage_app.modules.folder.entity.Folder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FolderRepository extends JpaRepository<Folder, UUID> {

    // (1) Tìm folder theo id và chưa bị xóa
    Optional<Folder> findByIdAndIsDeletedFalse(UUID id);

    // (2) Tìm tất cả folder GỐC của workspace (parent = null, chưa xóa)
    List<Folder> findAllByWorkspaceIdAndParentIsNullAndIsDeletedFalse(
            UUID workspaceId
    );

    // (3) Tìm tất cả folder CON trực tiếp của 1 folder cha
    List<Folder> findAllByParentIdAndIsDeletedFalse(UUID parentId);

    // (4) Tìm folder theo tên trong cùng folder cha — kiểm tra trùng tên
    Optional<Folder> findByWorkspaceIdAndParentIdAndNameAndIsDeletedFalse(
            UUID workspaceId,
            UUID parentId,
            String name
    );

    // (5) Tìm folder gốc cùng tên (parent = null) — case đặc biệt
    @Query("""
        SELECT f FROM Folder f
        WHERE f.workspace.id = :workspaceId
        AND f.parent IS NULL
        AND f.name = :name
        AND f.isDeleted = false
    """)
    Optional<Folder> findRootFolderByName(
            @Param("workspaceId") UUID workspaceId,
            @Param("name") String name
    );

    // (6) Soft delete folder và toàn bộ con cháu bằng recursive CTE
    // (Common Table Expression — PostgreSQL feature)
    @Modifying
    @Query(value = """
        WITH RECURSIVE folder_tree AS (
            SELECT id FROM folders WHERE id = :folderId
            UNION ALL
            SELECT f.id FROM folders f
            INNER JOIN folder_tree ft ON f.parent_id = ft.id
        )
        UPDATE folders
        SET is_deleted = true, deleted_at = NOW()
        WHERE id IN (SELECT id FROM folder_tree)
    """, nativeQuery = true)  // (7) Dùng native SQL vì JPQL không hỗ trợ CTE
    void softDeleteWithChildren(@Param("folderId") UUID folderId);

    // (8) Đếm số folder con trực tiếp — dùng cho response
    long countByParentIdAndIsDeletedFalse(UUID parentId);

    // (9) Kiểm tra folder có tồn tại và thuộc workspace không
    boolean existsByIdAndWorkspaceIdAndIsDeletedFalse(
            UUID folderId,
            UUID workspaceId
    );

    // (1) Đếm folder trong workspace
    long countByWorkspaceIdAndIsDeletedFalse(UUID workspaceId);

    // (2) Đếm tổng folder của user
    @Query("""
    SELECT COUNT(f) FROM Folder f
    WHERE f.createdByUser.id = :userId
    AND f.isDeleted = false
""")
    long countByCreatedByUserIdAndNotDeleted(@Param("userId") UUID userId);
}