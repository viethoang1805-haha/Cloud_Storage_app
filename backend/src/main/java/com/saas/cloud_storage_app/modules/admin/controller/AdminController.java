package com.saas.cloud_storage_app.modules.admin.controller;

import com.saas.cloud_storage_app.common.exception.AppException;
import com.saas.cloud_storage_app.common.exception.ErrorCode;
import com.saas.cloud_storage_app.common.response.ApiResponse;
import com.saas.cloud_storage_app.modules.dashboard.dto.response.SystemDashboardResponse;
import com.saas.cloud_storage_app.modules.dashboard.service.DashboardService;
import com.saas.cloud_storage_app.modules.user.dto.response.UserResponse;
import com.saas.cloud_storage_app.modules.user.entity.User;
import com.saas.cloud_storage_app.modules.user.mapper.UserMapper;
import com.saas.cloud_storage_app.modules.user.repository.UserRepository;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Tag(name = "Admin", description = "API quản trị hệ thống")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final DashboardService dashboardService;
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<SystemDashboardResponse>> getSystemDashboard() {
        try {
            SystemDashboardResponse response = dashboardService.getSystemDashboard();
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            log.error("Admin dashboard error: ", e);
            throw e;  // GlobalExceptionHandler sẽ bắt
        }
    }

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers() {
        List<UserResponse> users = userRepository.findAll()
                .stream()
                .map(userMapper::toUserResponse)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(users));
    }
    // Thêm vào AdminController.java:

    @PatchMapping("/users/{userId}/toggle")
    public ResponseEntity<ApiResponse<Void>> toggleUserStatus(
            @PathVariable UUID userId,
            @RequestBody Map<String, Boolean> body) {

        Boolean enabled = body.get("enabled");
        if (enabled == null) {
            throw new AppException(ErrorCode.VALIDATION_FAILED,
                    "Thiếu trường 'enabled'");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        user.setEnabled(enabled);
        userRepository.save(user);

        log.info("Toggle user {} → enabled={}", user.getEmail(), enabled);

        return ResponseEntity.ok(ApiResponse.success(
                enabled ? "Đã mở khóa tài khoản" : "Đã khóa tài khoản"
        ));
    }
}