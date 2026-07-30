package com.saas.cloud_storage_app.modules.share.mapper;

import com.saas.cloud_storage_app.common.utils.FileUtils;
import com.saas.cloud_storage_app.modules.share.dto.response.FilePermissionResponse;
import com.saas.cloud_storage_app.modules.share.dto.response.PublicFileResponse;
import com.saas.cloud_storage_app.modules.share.dto.response.ShareLinkResponse;
import com.saas.cloud_storage_app.modules.share.entity.FilePermission;
import com.saas.cloud_storage_app.modules.share.entity.ShareLink;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ShareMapper {

    // (1) Frontend URL — dùng để build share URL đầy đủ
    @Value("${app.frontend-url:http://localhost:5173}")
    private String frontendUrl;

    public ShareLinkResponse toShareLinkResponse(ShareLink shareLink) {
        var file = shareLink.getFile();

        return ShareLinkResponse.builder()
                .id(shareLink.getId().toString())
                .token(shareLink.getToken())
                // (2) Build full URL: http://localhost:5173/share/xK9mP2
                .shareUrl(frontendUrl + "/share/" + shareLink.getToken())
                .hasPassword(shareLink.hasPassword())
                .expiresAt(shareLink.getExpiresAt())
                .isActive(shareLink.isActive())
                .downloadCount(shareLink.getDownloadCount())
                .maxDownloads(shareLink.getMaxDownloads())
                .createdAt(shareLink.getCreatedAt())
                .file(ShareLinkResponse.FileInfo.builder()
                        .id(file.getId().toString())
                        .originalName(file.getOriginalName())
                        .contentType(file.getContentType())
                        .size(file.getSize())
                        .sizeFormatted(FileUtils.formatSize(file.getSize()))
                        .build()
                )
                .build();
    }

    // (3) Build response khi truy cập public link
    public PublicFileResponse toPublicFileResponse(
            ShareLink shareLink,
            boolean passwordVerified,
            String downloadUrl) {

        var file = shareLink.getFile();

        // (4) Chỉ include file detail khi đã verify password
        PublicFileResponse.FileDetail fileDetail = null;
        if (passwordVerified || !shareLink.hasPassword()) {
            fileDetail = PublicFileResponse.FileDetail.builder()
                    .originalName(file.getOriginalName())
                    .contentType(file.getContentType())
                    .size(file.getSize())
                    .sizeFormatted(FileUtils.formatSize(file.getSize()))
                    .isImage(file.isImage())
                    .build();
        }

        return PublicFileResponse.builder()
                .token(shareLink.getToken())
                .hasPassword(shareLink.hasPassword())
                .passwordVerified(passwordVerified)
                .expiresAt(shareLink.getExpiresAt())
                .downloadCount(shareLink.getDownloadCount())
                .maxDownloads(shareLink.getMaxDownloads())
                .fileDetail(fileDetail)
                .downloadUrl(passwordVerified || !shareLink.hasPassword()
                        ? downloadUrl
                        : null   // (5) không trả download URL nếu chưa verify
                )
                .build();
    }

    public FilePermissionResponse toPermissionResponse(FilePermission permission) {
        return FilePermissionResponse.builder()
                .id(permission.getId().toString())
                .permission(permission.getPermission().name())
                .isExpired(!permission.isValid())
                .expiresAt(permission.getExpiresAt())
                .createdAt(permission.getCreatedAt())
                .user(toSharedUserInfo(permission.getUser()))
                .sharedBy(toSharedUserInfo(permission.getSharedBy()))
                .build();
    }

    private FilePermissionResponse.SharedUserInfo toSharedUserInfo(
            com.saas.cloud_storage_app.modules.user.entity.User user) {
        return FilePermissionResponse.SharedUserInfo.builder()
                .id(user.getId().toString())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .avatarUrl(user.getAvatarUrl())
                .build();
    }
}