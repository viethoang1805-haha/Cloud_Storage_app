package com.saas.cloud_storage_app.modules.notification.websocket;

import com.saas.cloud_storage_app.modules.notification.dto.response.NotificationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationSocketHandler {

    // (1) Template để gửi message qua WebSocket
    private final SimpMessagingTemplate messagingTemplate;

    // (1) Nhận email làm destinationUser
    // Spring dùng principal.getName() = email để route
    public void sendToUser(String email, NotificationResponse notification) {
        try {
            messagingTemplate.convertAndSendToUser(
                    email,                      // (2) phải là email, không phải UUID
                    "/queue/notifications",
                    notification
            );
            log.debug("Đã push notification đến: {}", email);
        } catch (Exception e) {
            log.error("Lỗi push notification đến {}: {}", email, e.getMessage());
        }
    }

    // (5) Broadcast đến tất cả user trong workspace
    public void broadcastToWorkspace(String workspaceId,
                                     NotificationResponse notification) {
        try {
            messagingTemplate.convertAndSend(
                    "/topic/workspace/" + workspaceId,  // topic chung
                    notification
            );
        } catch (Exception e) {
            log.warn("Broadcast workspace {} thất bại: {}",
                    workspaceId, e.getMessage());
        }
    }
}