package com.saas.cloud_storage_app.modules.folder.controller;

import com.saas.cloud_storage_app.common.response.ApiResponse;
import com.saas.cloud_storage_app.modules.folder.dto.request.FolderCreateRequest;
import com.saas.cloud_storage_app.modules.folder.dto.request.FolderMoveRequest;
import com.saas.cloud_storage_app.modules.folder.dto.request.FolderRenameRequest;
import com.saas.cloud_storage_app.modules.folder.dto.response.FolderResponse;
import com.saas.cloud_storage_app.modules.folder.dto.response.FolderTreeResponse;
import com.saas.cloud_storage_app.modules.folder.service.FolderService;
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
@RequestMapping("/api/v1/workspaces/{workspaceId}/folders")
@RequiredArgsConstructor
@Tag(name = "Folder", description = "API quản lý thư mục")
@SecurityRequirement(name = "bearerAuth")
public class FolderController {

    private final FolderService folderService;

    @PostMapping
    @Operation(summary = "Tạo folder mới")
    public ResponseEntity<ApiResponse<FolderResponse>> createFolder(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID workspaceId,
            @Valid @RequestBody FolderCreateRequest request
    ) {
        FolderResponse response = folderService.createFolder(
                userDetails.getUsername(), workspaceId, request
        );
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success( response,"Tạo folder thành công"));
    }

    @GetMapping
    @Operation(summary = "Danh sách folder gốc của workspace")
    public ResponseEntity<ApiResponse<List<FolderResponse>>> getRootFolders(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID workspaceId
    ) {
        List<FolderResponse> response = folderService
                .getRootFolders(userDetails.getUsername(), workspaceId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{folderId}")
    @Operation(summary = "Chi tiết folder")
    public ResponseEntity<ApiResponse<FolderResponse>> getFolderById(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID workspaceId,
            @PathVariable UUID folderId
    ) {
        FolderResponse response = folderService.getFolderById(
                userDetails.getUsername(), workspaceId, folderId
        );
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{folderId}/tree")
    @Operation(summary = "Lấy toàn bộ cây thư mục con")
    public ResponseEntity<ApiResponse<FolderTreeResponse>> getFolderTree(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID workspaceId,
            @PathVariable UUID folderId
    ) {
        FolderTreeResponse response = folderService.getFolderTree(
                userDetails.getUsername(), workspaceId, folderId
        );
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{folderId}")
    @Operation(summary = "Đổi tên folder")
    public ResponseEntity<ApiResponse<FolderResponse>> renameFolder(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID workspaceId,
            @PathVariable UUID folderId,
            @Valid @RequestBody FolderRenameRequest request
    ) {
        FolderResponse response = folderService.renameFolder(
                userDetails.getUsername(), workspaceId, folderId, request
        );
        return ResponseEntity.ok(ApiResponse.success(response,"Đổi tên thành công"));
    }

    @PatchMapping("/{folderId}/move")  // (1)
    @Operation(summary = "Di chuyển folder")
    public ResponseEntity<ApiResponse<FolderResponse>> moveFolder(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID workspaceId,
            @PathVariable UUID folderId,
            @RequestBody FolderMoveRequest request  // (2) không có @Valid vì không có constraint
    ) {
        FolderResponse response = folderService.moveFolder(
                userDetails.getUsername(), workspaceId, folderId, request
        );
        return ResponseEntity.ok(ApiResponse.success(response,"Di chuyển folder thành công"));
    }

    @DeleteMapping("/{folderId}")
    @Operation(summary = "Xóa folder (soft delete)")
    public ResponseEntity<ApiResponse<Void>> deleteFolder(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID workspaceId,
            @PathVariable UUID folderId
    ) {
        folderService.deleteFolder(
                userDetails.getUsername(), workspaceId, folderId
        );
        return ResponseEntity.ok(ApiResponse.success("Xóa folder thành công"));
    }
}