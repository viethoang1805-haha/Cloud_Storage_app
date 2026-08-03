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

    Optional<Folder> findByIdAndIsDeletedFalse(UUID id);

    List<Folder> findAllByWorkspaceIdAndParentIsNullAndIsDeletedFalse(UUID workspaceId);

    List<Folder> findAllByParentIdAndIsDeletedFalse(UUID parentId);

    Optional<Folder> findByWorkspaceIdAndParentIdAndNameAndIsDeletedFalse(
            UUID workspaceId, UUID parentId, String name);

    @Query("""
        SELECT f FROM Folder f
        WHERE f.workspace.id = :workspaceId
        AND f.parent IS NULL
        AND f.name = :name
        AND f.isDeleted = false
    """)
    Optional<Folder> findRootFolderByName(
            @Param("workspaceId") UUID workspaceId,
            @Param("name") String name);

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
    """, nativeQuery = true)
    void softDeleteWithChildren(@Param("folderId") UUID folderId);

    long countByParentIdAndIsDeletedFalse(UUID parentId);

    boolean existsByIdAndWorkspaceIdAndIsDeletedFalse(UUID folderId, UUID workspaceId);

    // Dashboard methods
    long countByWorkspaceIdAndIsDeletedFalse(UUID workspaceId);

    @Query("""
        SELECT COUNT(f) FROM Folder f
        WHERE f.createdByUser.id = :userId
        AND f.isDeleted = false
    """)
    long countByCreatedByUserIdAndNotDeleted(@Param("userId") UUID userId);
}