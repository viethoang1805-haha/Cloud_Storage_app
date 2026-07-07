package com.saas.cloud_storage_app.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.saas.cloud_storage_app.common.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class AccessDeniedHandlerImpl implements AccessDeniedHandler {

    private final ObjectMapper objectMapper; // (1) dùng để convert object → JSON string

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException
    ) throws IOException {

        // (2) Set response là JSON
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpServletResponse.SC_FORBIDDEN); // 403

        // (3) Tạo ApiResponse lỗi và ghi vào response body
        ApiResponse<Void> apiResponse = ApiResponse.error(
                403, "Bạn không có quyền thực hiện thao tác này"
        );

        response.getWriter().write(
                objectMapper.writeValueAsString(apiResponse)
        );
    }
}