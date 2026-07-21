package com.saas.cloud_storage_app.modules.workspace.controller;

import com.saas.cloud_storage_app.common.response.ApiResponse;
import com.saas.cloud_storage_app.modules.workspace.dto.request.WorkspaceCreateRequest;
import com.saas.cloud_storage_app.modules.workspace.dto.request.WorkspaceUpdateRequest;
import com.saas.cloud_storage_app.modules.workspace.dto.response.WorkspaceResponse;
import com.saas.cloud_storage_app.modules.workspace.service.WorkspaceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/workspaces")
@RequiredArgsConstructor
@Tag(name = "Workspace", description = "API quản lý workspace")
@SecurityRequirement(name = "bearerAuth")
public class WorkspaceController {

    private final WorkspaceService workspaceService;

    @PostMapping
    @Operation(summary = "Tạo workspace mới")
    public ResponseEntity<ApiResponse<WorkspaceResponse>> createWorkspace(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody WorkspaceCreateRequest request
    ) {
        WorkspaceResponse response = workspaceService
                .createWorkspace(userDetails.getUsername(), request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Tạo workspace thành công" ));
    }

    @GetMapping
    @Operation(summary = "Danh sách workspace của tôi")
    public ResponseEntity<ApiResponse<List<WorkspaceResponse>>> getMyWorkspaces(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        List<WorkspaceResponse> response = workspaceService
                .getMyWorkspaces(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{workspaceId}")
    @Operation(summary = "Chi tiết workspace")
    public ResponseEntity<ApiResponse<WorkspaceResponse>> getWorkspace(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID workspaceId  // (1)
    ) {
        WorkspaceResponse response = workspaceService
                .getWorkspaceById(userDetails.getUsername(), workspaceId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{workspaceId}")
    @Operation(summary = "Cập nhật workspace")
    public ResponseEntity<ApiResponse<WorkspaceResponse>> updateWorkspace(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID workspaceId,
            @Valid @RequestBody WorkspaceUpdateRequest request
    ) {
        WorkspaceResponse response = workspaceService
                .updateWorkspace(userDetails.getUsername(), workspaceId, request);
        return ResponseEntity.ok(ApiResponse.success(response,"Cập nhật thành công" ));
    }

    @DeleteMapping("/{workspaceId}")
    @Operation(summary = "Xóa workspace")
    public ResponseEntity<ApiResponse<Void>> deleteWorkspace(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID workspaceId
    ) {
        workspaceService.deleteWorkspace(userDetails.getUsername(), workspaceId);
        return ResponseEntity.ok(ApiResponse.success("Xóa workspace thành công"));
    }
}