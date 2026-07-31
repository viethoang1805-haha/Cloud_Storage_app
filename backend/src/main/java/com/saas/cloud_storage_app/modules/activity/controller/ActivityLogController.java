package com.saas.cloud_storage_app.modules.activity.controller;

import com.saas.cloud_storage_app.common.response.ApiResponse;
import com.saas.cloud_storage_app.modules.activity.dto.request.ActivityFilterRequest;
import com.saas.cloud_storage_app.modules.activity.dto.response.ActivityLogPageResponse;
import com.saas.cloud_storage_app.modules.activity.service.ActivityLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Tag(name = "Activity Log", description = "API nhật ký hoạt động")
@SecurityRequirement(name = "bearerAuth")
public class ActivityLogController {

    private final ActivityLogService activityLogService;

    // (1) Log của workspace — tất cả member xem được
    @GetMapping("/api/v1/workspaces/{workspaceId}/activities")
    @Operation(summary = "Nhật ký hoạt động workspace")
    public ResponseEntity<ApiResponse<ActivityLogPageResponse>> getWorkspaceActivities(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID workspaceId,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String targetType,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime fromDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime toDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        // (2) Build filter từ query params
        ActivityFilterRequest filter = new ActivityFilterRequest();
        filter.setAction(action);
        filter.setTargetType(targetType);
        filter.setFromDate(fromDate);
        filter.setToDate(toDate);
        filter.setPage(page);
        filter.setSize(size);

        ActivityLogPageResponse response = activityLogService
                .getWorkspaceActivities(
                        userDetails.getUsername(), workspaceId, filter
                );

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // Log của tôi trong workspace
    @GetMapping("/api/v1/workspaces/{workspaceId}/activities/mine")
    @Operation(summary = "Nhật ký hoạt động của tôi trong workspace")
    public ResponseEntity<ApiResponse<ActivityLogPageResponse>> getMyActivities(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID workspaceId,
            @RequestParam(required = false) String action,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        ActivityFilterRequest filter = new ActivityFilterRequest();
        filter.setAction(action);
        filter.setPage(page);
        filter.setSize(size);

        ActivityLogPageResponse response = activityLogService
                .getMyActivities(
                        userDetails.getUsername(), workspaceId, filter
                );

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // (3) Admin only — xem toàn bộ log hệ thống
    @GetMapping("/api/v1/admin/activities")
    @Operation(summary = "Toàn bộ nhật ký hệ thống (Admin)")
    @PreAuthorize("hasRole('ADMIN')")  // (4) Chỉ ADMIN mới vào được
    public ResponseEntity<ApiResponse<ActivityLogPageResponse>> getAllActivities(
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String targetType,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime fromDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime toDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size
    ) {
        ActivityFilterRequest filter = new ActivityFilterRequest();
        filter.setAction(action);
        filter.setTargetType(targetType);
        filter.setFromDate(fromDate);
        filter.setToDate(toDate);
        filter.setPage(page);
        filter.setSize(size);

        ActivityLogPageResponse response = activityLogService
                .getAllActivities(filter);

        return ResponseEntity.ok(ApiResponse.success(response));
    }
}