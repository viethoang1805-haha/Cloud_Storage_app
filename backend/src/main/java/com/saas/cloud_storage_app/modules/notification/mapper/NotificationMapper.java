package com.saas.cloud_storage_app.modules.notification.mapper;

import com.saas.cloud_storage_app.modules.notification.dto.response.NotificationResponse;
import com.saas.cloud_storage_app.modules.notification.entity.Notification;
import org.springframework.stereotype.Component;

@Component
public class NotificationMapper {

    public NotificationResponse toResponse(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId().toString())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .type(notification.getType())
                .isRead(notification.isRead())
                .readAt(notification.getReadAt())
                .refType(notification.getRefType())
                .refId(notification.getRefId())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}