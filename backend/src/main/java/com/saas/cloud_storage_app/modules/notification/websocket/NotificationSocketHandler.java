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

    // (2) Gửi notification đến 1 user cụ thể
    public void sendToUser(String userId, NotificationResponse notification) {
        try {
            // (3) Gửi đến destination: /user/{userId}/queue/notifications
            // Chỉ user có userId này mới nhận được
            messagingTemplate.convertAndSendToUser(
                    userId,                     // user identifier
                    "/queue/notifications",     // destination suffix
                    notification                // payload (auto serialize to JSON)
            );

            log.debug("Gửi notification đến user: {}", userId);

        } catch (Exception e) {
            // (4) Không throw — user có thể offline
            // Notification đã lưu DB rồi, user online lại sẽ poll
            log.warn("Không gửi được WebSocket đến user {}: {}",
                    userId, e.getMessage());
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