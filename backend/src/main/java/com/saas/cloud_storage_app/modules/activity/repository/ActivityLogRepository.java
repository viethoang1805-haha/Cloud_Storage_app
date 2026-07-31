package com.saas.cloud_storage_app.modules.activity.repository;

import com.saas.cloud_storage_app.modules.activity.entity.ActivityLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface ActivityLogRepository extends JpaRepository<ActivityLog, UUID> {

    // (1) Tất cả log của workspace — sắp xếp theo thời gian
    Page<ActivityLog> findAllByWorkspaceIdOrderByCreatedAtDesc(
            UUID workspaceId, Pageable pageable
    );

    // (2) Log của 1 actor trong workspace
    Page<ActivityLog> findAllByWorkspaceIdAndActorIdOrderByCreatedAtDesc(
            UUID workspaceId, UUID actorId, Pageable pageable
    );

    // (3) Log theo loại action
    Page<ActivityLog> findAllByWorkspaceIdAndActionOrderByCreatedAtDesc(
            UUID workspaceId, String action, Pageable pageable
    );

    // (4) Log theo target object
    Page<ActivityLog> findAllByTargetIdOrderByCreatedAtDesc(
            UUID targetId, Pageable pageable
    );

    // (5) Tìm kiếm với nhiều filter kết hợp
    @Query("""
        SELECT a FROM ActivityLog a
        WHERE (:workspaceId IS NULL OR a.workspaceId = :workspaceId)
        AND (:actorId IS NULL OR a.actor.id = :actorId)
        AND (:action IS NULL OR a.action = :action)
        AND (:targetType IS NULL OR a.targetType = :targetType)
        AND (:fromDate IS NULL OR a.createdAt >= :fromDate)
        AND (:toDate IS NULL OR a.createdAt <= :toDate)
        ORDER BY a.createdAt DESC
    """)
    Page<ActivityLog> search(
            @Param("workspaceId") UUID workspaceId,
            @Param("actorId") UUID actorId,
            @Param("action") String action,
            @Param("targetType") String targetType,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate,
            Pageable pageable
    );

    // (6) Đếm theo action type trong workspace — dùng cho dashboard
    @Query("""
        SELECT a.action, COUNT(a)
        FROM ActivityLog a
        WHERE a.workspaceId = :workspaceId
        AND a.createdAt >= :fromDate
        GROUP BY a.action
    """)
    java.util.List<Object[]> countByActionInWorkspace(
            @Param("workspaceId") UUID workspaceId,
            @Param("fromDate") LocalDateTime fromDate
    );

    // (1) Đếm activity theo action trong khoảng thời gian và workspace
    @Query("""
    SELECT DATE(a.createdAt) as date,
           a.action,
           COUNT(a)
    FROM ActivityLog a
    WHERE a.workspaceId = :workspaceId
    AND a.createdAt >= :fromDate
    AND a.action IN ('FILE_UPLOADED', 'FILE_DOWNLOADED', 'FILE_DELETED')
    GROUP BY DATE(a.createdAt), a.action
    ORDER BY DATE(a.createdAt) ASC
""")
    List<Object[]> getDailyActivityStats(
            @Param("workspaceId") UUID workspaceId,
            @Param("fromDate") LocalDateTime fromDate
    );

    // (2) Lấy activity gần đây của workspace
    List<ActivityLog> findTop10ByWorkspaceIdOrderByCreatedAtDesc(UUID workspaceId);
}