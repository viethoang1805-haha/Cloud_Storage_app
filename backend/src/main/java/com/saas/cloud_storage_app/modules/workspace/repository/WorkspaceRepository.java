package com.saas.cloud_storage_app.modules.workspace.repository;

import com.saas.cloud_storage_app.modules.workspace.entity.Workspace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WorkspaceRepository extends JpaRepository<Workspace, UUID> {

    // (1) Tìm tất cả workspace mà user là OWNER
    List<Workspace> findAllByOwnerId(UUID ownerId);

    // (2) Tìm tất cả workspace mà user là MEMBER (kể cả OWNER)
    // Dùng JPQL với JOIN để tìm qua bảng workspace_members
    @Query("""
        SELECT DISTINCT w FROM Workspace w
        JOIN WorkspaceMember wm ON wm.workspace.id = w.id
        WHERE wm.user.id = :userId
        ORDER BY w.createdAt DESC
    """)  // (3) Text Block Java 15+ — viết JPQL nhiều dòng cho dễ đọc
    List<Workspace> findAllByMemberId(@Param("userId") UUID userId);

    // (4) Tìm workspace cá nhân của user
    Optional<Workspace> findByOwnerIdAndIsPersonalTrue(UUID ownerId);

    // (5) Kiểm tra user có phải owner không — dùng trước khi xóa
    boolean existsByIdAndOwnerId(UUID workspaceId, UUID ownerId);
    // (1) Đếm tổng workspace
    long count();

    // (2) Top workspace theo số file
    @Query("""
    SELECT w.id, w.name, COUNT(f), SUM(f.size)
    FROM Workspace w
    LEFT JOIN FileEntity f ON f.workspace.id = w.id
    AND f.isDeleted = false
    GROUP BY w.id, w.name
    ORDER BY SUM(f.size) DESC NULLS LAST
""")
    List<Object[]> findTopWorkspacesByStorage(Pageable pageable);
}