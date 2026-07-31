package com.saas.cloud_storage_app.modules.notification.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class NotificationPageResponse {

    private List<NotificationResponse> notifications;
    private long unreadCount;    // (1) tổng số chưa đọc — dùng cho badge
    private int currentPage;
    private int totalPages;
    private long totalElements;
    private boolean hasNext;
}