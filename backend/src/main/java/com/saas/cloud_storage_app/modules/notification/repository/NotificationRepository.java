package com.saas.cloud_storage_app.modules.notification.repository;

import com.saas.cloud_storage_app.modules.notification.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    // (1) Tất cả notification của user, phân trang
    Page<Notification> findAllByUserIdOrderByCreatedAtDesc(
            UUID userId, Pageable pageable
    );

    // (2) Chỉ notification chưa đọc
    Page<Notification> findAllByUserIdAndIsReadFalseOrderByCreatedAtDesc(
            UUID userId, Pageable pageable
    );

    // (3) Đếm số chưa đọc — dùng cho badge số
    long countByUserIdAndIsReadFalse(UUID userId);

    // (4) Đánh dấu 1 notification đã đọc
    @Modifying
    @Query("""
        UPDATE Notification n
        SET n.isRead = true, n.readAt = CURRENT_TIMESTAMP
        WHERE n.id = :id AND n.user.id = :userId
    """)
    void markAsRead(
            @Param("id") UUID id,
            @Param("userId") UUID userId
    );

    // (5) Đánh dấu TẤT CẢ đã đọc — "Mark all as read"
    @Modifying
    @Query("""
        UPDATE Notification n
        SET n.isRead = true, n.readAt = CURRENT_TIMESTAMP
        WHERE n.user.id = :userId AND n.isRead = false
    """)
    void markAllAsRead(@Param("userId") UUID userId);

    // (6) Xóa notification cũ hơn 30 ngày — cleanup job
    @Modifying
    @Query("""
        DELETE FROM Notification n
        WHERE n.user.id = :userId
        AND n.createdAt < :cutoffDate
    """)
    void deleteOldNotifications(
            @Param("userId") UUID userId,
            @Param("cutoffDate") java.time.LocalDateTime cutoffDate
    );
}