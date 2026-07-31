package com.saas.cloud_storage_app.modules.notification.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class NotificationResponse {

    private String id;
    private String title;
    private String message;
    private String type;
    private boolean isRead;
    private LocalDateTime readAt;
    private String refType;     // "FILE", "WORKSPACE", "MEMBER"
    private UUID refId;         // id để navigate
    private LocalDateTime createdAt;
}