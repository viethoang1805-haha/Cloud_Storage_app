package com.saas.cloud_storage_app.modules.auth.repository;

import com.saas.cloud_storage_app.modules.auth.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    // Tìm refresh token theo chuỗi token — dùng khi client gửi lên để refresh
    Optional<RefreshToken> findByToken(String token);

    // Tìm tất cả refresh token của 1 user — dùng khi logout all devices
    java.util.List<RefreshToken> findAllByUserId(UUID userId);

    // (1) Thu hồi tất cả token của user — dùng khi đổi mật khẩu hoặc logout all
    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.isRevoked = true WHERE rt.user.id = :userId")
    void revokeAllByUserId(@Param("userId") UUID userId);

    // (2) Xóa token đã hết hạn hoặc bị revoke — dùng cho cleanup job
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.user.id = :userId AND (rt.isRevoked = true OR rt.expiresAt < CURRENT_TIMESTAMP)")
    void deleteExpiredOrRevokedByUserId(@Param("userId") UUID userId);
}