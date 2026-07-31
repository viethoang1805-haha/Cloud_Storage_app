package com.saas.cloud_storage_app.modules.notification.service;

import com.saas.cloud_storage_app.modules.notification.dto.response.NotificationPageResponse;
import com.saas.cloud_storage_app.modules.notification.event.FileSharedEvent;
import com.saas.cloud_storage_app.modules.notification.event.FileUploadedEvent;
import com.saas.cloud_storage_app.modules.notification.event.MemberInvitedEvent;

import java.util.UUID;

public interface NotificationService {

    // REST API methods
    NotificationPageResponse getMyNotifications(
            String email, int page, int size, boolean unreadOnly);

    void markAsRead(String email, UUID notificationId);

    void markAllAsRead(String email);

    long getUnreadCount(String email);

    // Event handlers — gọi từ @EventListener
    void handleFileUploaded(FileUploadedEvent event);
    void handleMemberInvited(MemberInvitedEvent event);
    void handleFileShared(FileSharedEvent event);
}