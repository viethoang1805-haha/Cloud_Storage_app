package com.saas.cloud_storage_app.modules.dashboard.controller;

import com.saas.cloud_storage_app.common.response.ApiResponse;
import com.saas.cloud_storage_app.modules.dashboard.dto.response.PersonalDashboardResponse;
import com.saas.cloud_storage_app.modules.dashboard.dto.response.SystemDashboardResponse;
import com.saas.cloud_storage_app.modules.dashboard.dto.response.WorkspaceDashboardResponse;
import com.saas.cloud_storage_app.modules.dashboard.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "API thống kê và tổng quan")
@SecurityRequirement(name = "bearerAuth")
public class DashboardController {

    private final DashboardService dashboardService;

    // (1) Dashboard cá nhân
    @GetMapping("/api/v1/dashboard")
    @Operation(summary = "Tổng quan cá nhân")
    public ResponseEntity<ApiResponse<PersonalDashboardResponse>> getPersonalDashboard(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        PersonalDashboardResponse response = dashboardService
                .getPersonalDashboard(userDetails.getUsername());

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // (2) Dashboard workspace
    @GetMapping("/api/v1/workspaces/{workspaceId}/dashboard")
    @Operation(summary = "Tổng quan workspace")
    public ResponseEntity<ApiResponse<WorkspaceDashboardResponse>> getWorkspaceDashboard(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID workspaceId
    ) {
        WorkspaceDashboardResponse response = dashboardService
                .getWorkspaceDashboard(userDetails.getUsername(), workspaceId);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // (3) Dashboard hệ thống — admin only
    @GetMapping("/api/v1/admin/dashboard")
    @Operation(summary = "Tổng quan hệ thống (Admin)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<SystemDashboardResponse>> getSystemDashboard() {
        SystemDashboardResponse response = dashboardService.getSystemDashboard();
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}