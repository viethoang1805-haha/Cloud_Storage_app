// Tạo AdminController.java
package com.saas.cloud_storage_app.modules.admin.controller;

import com.saas.cloud_storage_app.common.response.ApiResponse;
import com.saas.cloud_storage_app.modules.dashboard.dto.response.SystemDashboardResponse;
import com.saas.cloud_storage_app.modules.dashboard.service.DashboardService;
import com.saas.cloud_storage_app.modules.user.dto.response.UserResponse;
import com.saas.cloud_storage_app.modules.user.repository.UserRepository;
import com.saas.cloud_storage_app.modules.user.mapper.UserMapper;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Tag(name = "Admin", description = "API quản trị hệ thống")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final DashboardService dashboardService;
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<SystemDashboardResponse>> getSystemDashboard() {
        return ResponseEntity.ok(
                ApiResponse.success(dashboardService.getSystemDashboard())
        );
    }

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers() {
        List<UserResponse> users = userRepository.findAll()
                .stream()
                .map(userMapper::toUserResponse)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(users));
    }
}