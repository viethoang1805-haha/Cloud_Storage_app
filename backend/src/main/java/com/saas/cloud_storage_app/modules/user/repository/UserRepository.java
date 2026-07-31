package com.saas.cloud_storage_app.modules.user.repository;

import com.saas.cloud_storage_app.modules.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    // (1) Tìm user theo email — dùng cho login và CustomUserDetailsService
    Optional<User> findByEmail(String email);

    // (2) Kiểm tra email đã tồn tại chưa — dùng khi đăng ký
    boolean existsByEmail(String email);

    // (3) Cập nhật storage dùng JPQL trực tiếp — không cần load cả entity
    @Modifying  // (4)
    @Query("UPDATE User u SET u.storageUsed = u.storageUsed + :size WHERE u.id = :userId")
    void increaseStorageUsed(@Param("userId") UUID userId, @Param("size") Long size);

    @Modifying
    @Query("UPDATE User u SET u.storageUsed = GREATEST(0, u.storageUsed - :size) WHERE u.id = :userId")
    void decreaseStorageUsed(@Param("userId") UUID userId, @Param("size") Long size);
    // (1) Đếm user mới trong N ngày
    @Query("""
    SELECT COUNT(u) FROM User u
    WHERE u.createdAt >= :fromDate
""")
    long countUsersCreatedAfter(@Param("fromDate") LocalDateTime fromDate);

    // (2) Tổng số user
    long count();
}