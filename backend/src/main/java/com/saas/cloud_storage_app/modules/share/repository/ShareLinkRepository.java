package com.saas.cloud_storage_app.modules.share.repository;

import com.saas.cloud_storage_app.modules.share.entity.ShareLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ShareLinkRepository extends JpaRepository<ShareLink, UUID> {

    // (1) Tìm link theo token — dùng khi truy cập public link
    Optional<ShareLink> findByToken(String token);

    // (2) Tìm tất cả link của 1 file
    List<ShareLink> findAllByFileId(UUID fileId);

    // (3) Tìm link active của file (1 file nên chỉ có 1 link active)
    Optional<ShareLink> findByFileIdAndIsActiveTrue(UUID fileId);

    // (4) Kiểm tra file đã có share link chưa
    boolean existsByFileIdAndIsActiveTrue(UUID fileId);

    // (5) Vô hiệu hóa tất cả link của file — khi xóa file
    @Modifying
    @Query("UPDATE ShareLink sl SET sl.isActive = false WHERE sl.file.id = :fileId")
    void deactivateAllByFileId(@Param("fileId") UUID fileId);

    // (6) Tăng download count — atomic update tránh race condition
    @Modifying
    @Query("""
        UPDATE ShareLink sl
        SET sl.downloadCount = sl.downloadCount + 1
        WHERE sl.id = :linkId
    """)
    void incrementDownloadCount(@Param("linkId") UUID linkId);
}