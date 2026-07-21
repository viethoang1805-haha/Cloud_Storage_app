package com.saas.cloud_storage_app.modules.member.repository;

import com.saas.cloud_storage_app.common.enums.WorkspaceRole;
import com.saas.cloud_storage_app.modules.member.entity.WorkspaceMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WorkspaceMemberRepository extends JpaRepository<WorkspaceMember, UUID> {

    // (1) Tìm member record của 1 user trong 1 workspace cụ thể
    Optional<WorkspaceMember> findByWorkspaceIdAndUserId(
            UUID workspaceId, UUID userId
    );

    // (2) Tìm tất cả member của 1 workspace
    List<WorkspaceMember> findAllByWorkspaceId(UUID workspaceId);

    // (3) Kiểm tra user có phải member của workspace không
    boolean existsByWorkspaceIdAndUserId(UUID workspaceId, UUID userId);

    // (4) Đếm số member trong workspace — dùng cho dashboard stats
    long countByWorkspaceId(UUID workspaceId);

    // (5) Đổi role của member
    @Modifying
    @Query("""
        UPDATE WorkspaceMember wm
        SET wm.role = :role
        WHERE wm.workspace.id = :workspaceId
        AND wm.user.id = :userId
    """)
    void updateMemberRole(
            @Param("workspaceId") UUID workspaceId,
            @Param("userId") UUID userId,
            @Param("role") WorkspaceRole role
    );

    // (6) Xóa member khỏi workspace
    @Modifying
    @Query("""
        DELETE FROM WorkspaceMember wm
        WHERE wm.workspace.id = :workspaceId
        AND wm.user.id = :userId
    """)
    void deleteByWorkspaceIdAndUserId(
            @Param("workspaceId") UUID workspaceId,
            @Param("userId") UUID userId
    );
}