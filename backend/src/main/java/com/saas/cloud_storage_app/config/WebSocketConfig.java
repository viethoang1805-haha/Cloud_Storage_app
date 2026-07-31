package com.saas.cloud_storage_app.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Slf4j
@Configuration
@EnableWebSocketMessageBroker  // (1) Bật WebSocket với STOMP message broker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    // (2) Cấu hình message broker
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {

        // (3) Prefix cho server → client messages
        // Client subscribe: /topic/notifications/user-uuid
        registry.enableSimpleBroker("/topic", "/queue");

        // (4) Prefix cho client → server messages
        // Client gửi: /app/ping
        registry.setApplicationDestinationPrefixes("/app");

        // (5) Prefix cho message gửi đến user cụ thể
        // Server gửi: /user/{userId}/queue/notifications
        registry.setUserDestinationPrefix("/user");
    }

    // (6) Đăng ký STOMP endpoint
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry
                .addEndpoint("/ws")           // (7) URL kết nối: ws://localhost:8080/ws
                .setAllowedOriginPatterns("*") // (8) Cho phép tất cả origin khi dev
                .withSockJS();                 // (9) Fallback cho browser không hỗ trợ WS
    }
}