package com.saas.cloud_storage_app.config;

import com.saas.cloud_storage_app.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.messaging.simp.config.ChannelRegistration;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class WebSocketSecurityConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsService userDetailsService;

    // (1) Interceptor channel — xác thực JWT khi WebSocket kết nối

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(
                        message, StompHeaderAccessor.class);

                if (accessor != null &&
                        StompCommand.CONNECT.equals(accessor.getCommand())) {

                    String authHeader = accessor
                            .getFirstNativeHeader("Authorization");

                    if (authHeader != null && authHeader.startsWith("Bearer ")) {
                        String token = authHeader.substring(7);
                        try {
                            if (jwtTokenProvider.validateToken(token)) {
                                // (1) email từ JWT = principal name
                                String email = jwtTokenProvider.getEmailFromToken(token);

                                UserDetails userDetails = userDetailsService
                                        .loadUserByUsername(email);

                                UsernamePasswordAuthenticationToken auth =
                                        new UsernamePasswordAuthenticationToken(
                                                userDetails,
                                                null,
                                                userDetails.getAuthorities()
                                        );

                                // (2) Set auth — principal.getName() = email
                                accessor.setUser(auth);
                                log.info("WS auth OK: {}", email);
                            }
                        } catch (Exception e) {
                            log.warn("WS auth fail: {}", e.getMessage());
                        }
                    }
                }
                return message;
            }
        });
    }
}