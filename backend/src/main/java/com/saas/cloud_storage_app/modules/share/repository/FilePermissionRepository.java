package com.saas.cloud_storage_app.modules.share.repository;

import com.saas.cloud_storage_app.common.enums.PermissionType;
import com.saas.cloud_storage_app.modules.share.entity.FilePermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FilePermissionRepository extends JpaRepository<FilePermission, UUID> {

    // (1) Tìm permission của user với file cụ thể
    Optional<FilePermission> findByFileIdAndUserId(UUID fileId, UUID userId);

    // (2) Tìm tất cả permission của 1 file
    List<FilePermission> findAllByFileId(UUID fileId);

    // (3) Tìm tất cả file được chia sẻ với user
    List<FilePermission> findAllByUserId(UUID userId);

    // (4) Kiểm tra user có permission không
    boolean existsByFileIdAndUserId(UUID fileId, UUID userId);

    // (5) Cập nhật permission
    @Modifying
    @Query("""
        UPDATE FilePermission fp
        SET fp.permission = :permission
        WHERE fp.file.id = :fileId
        AND fp.user.id = :userId
    """)
    void updatePermission(
            @Param("fileId") UUID fileId,
            @Param("userId") UUID userId,
            @Param("permission") PermissionType permission
    );

    // (6) Xóa permission của user với file
    @Modifying
    @Query("""
        DELETE FROM FilePermission fp
        WHERE fp.file.id = :fileId
        AND fp.user.id = :userId
    """)
    void deleteByFileIdAndUserId(
            @Param("fileId") UUID fileId,
            @Param("userId") UUID userId
    );

    // (7) Xóa tất cả permission khi file bị xóa
    @Modifying
    @Query("DELETE FROM FilePermission fp WHERE fp.file.id = :fileId")
    void deleteAllByFileId(@Param("fileId") UUID fileId);
}