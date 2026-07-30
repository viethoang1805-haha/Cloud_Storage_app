package com.saas.cloud_storage_app.modules.share.controller;

import com.saas.cloud_storage_app.common.response.ApiResponse;
import com.saas.cloud_storage_app.modules.share.dto.request.*;
import com.saas.cloud_storage_app.modules.share.dto.response.*;
import com.saas.cloud_storage_app.modules.share.service.ShareService;
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
@RequiredArgsConstructor
@Tag(name = "Share", description = "API chia sẻ file")
public class ShareController {

    private final ShareService shareService;

    // =============================================
    // PUBLIC ENDPOINTS — không cần token
    // =============================================

    // (1) Truy cập file qua public link — không cần đăng nhập
    @PostMapping("/api/v1/share/public/{token}")
    @Operation(summary = "Truy cập file qua public share link")
    public ResponseEntity<ApiResponse<PublicFileResponse>> accessShareLink(
            @PathVariable String token,
            @RequestBody(required = false) AccessShareLinkRequest request
    ) {
        PublicFileResponse response = shareService.accessShareLink(token, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // =============================================
    // PROTECTED ENDPOINTS — cần token
    // =============================================

    // Tạo public share link
    @PostMapping("/api/v1/workspaces/{workspaceId}/files/{fileId}/share/link")
    @Operation(summary = "Tạo public share link cho file")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<ShareLinkResponse>> createShareLink(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID workspaceId,
            @PathVariable UUID fileId,
            @Valid @RequestBody(required = false) CreateShareLinkRequest request
    ) {
        // (2) request có thể null nếu không cần password/expiry
        CreateShareLinkRequest req = request != null
                ? request
                : new CreateShareLinkRequest();

        ShareLinkResponse response = shareService.createShareLink(
                userDetails.getUsername(), workspaceId, fileId, req
        );
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response,"Tạo share link thành công"));
    }

    // Lấy share link hiện tại của file
    @GetMapping("/api/v1/workspaces/{workspaceId}/files/{fileId}/share/link")
    @Operation(summary = "Lấy share link của file")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<ShareLinkResponse>> getShareLink(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID workspaceId,
            @PathVariable UUID fileId
    ) {
        ShareLinkResponse response = shareService.getShareLink(
                userDetails.getUsername(), workspaceId, fileId
        );
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // Vô hiệu hóa share link
    @DeleteMapping("/api/v1/workspaces/{workspaceId}/files/{fileId}/share/link")
    @Operation(summary = "Vô hiệu hóa share link")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<Void>> deactivateShareLink(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID workspaceId,
            @PathVariable UUID fileId
    ) {
        shareService.deactivateShareLink(
                userDetails.getUsername(), workspaceId, fileId
        );
        return ResponseEntity.ok(ApiResponse.success("Vô hiệu hóa link thành công"));
    }

    // Chia sẻ với user cụ thể
    @PostMapping("/api/v1/workspaces/{workspaceId}/files/{fileId}/share/users")
    @Operation(summary = "Chia sẻ file với user cụ thể")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<FilePermissionResponse>> shareWithUser(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID workspaceId,
            @PathVariable UUID fileId,
            @Valid @RequestBody ShareWithUserRequest request
    ) {
        FilePermissionResponse response = shareService.shareWithUser(
                userDetails.getUsername(), workspaceId, fileId, request
        );
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response,"Chia sẻ thành công"));
    }

    // Danh sách permission của file
    @GetMapping("/api/v1/workspaces/{workspaceId}/files/{fileId}/share/users")
    @Operation(summary = "Danh sách user được chia sẻ")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<List<FilePermissionResponse>>> getFilePermissions(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID workspaceId,
            @PathVariable UUID fileId
    ) {
        List<FilePermissionResponse> response = shareService.getFilePermissions(
                userDetails.getUsername(), workspaceId, fileId
        );
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // Cập nhật permission
    @PutMapping("/api/v1/workspaces/{workspaceId}/files/{fileId}/share/users/{userId}")
    @Operation(summary = "Cập nhật quyền của user")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<FilePermissionResponse>> updatePermission(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID workspaceId,
            @PathVariable UUID fileId,
            @PathVariable UUID userId,
            @Valid @RequestBody UpdatePermissionRequest request
    ) {
        FilePermissionResponse response = shareService.updatePermission(
                userDetails.getUsername(), workspaceId, fileId, userId, request
        );
        return ResponseEntity.ok(ApiResponse.success( response,"Cập nhật quyền thành công"));
    }

    // Thu hồi permission
    @DeleteMapping("/api/v1/workspaces/{workspaceId}/files/{fileId}/share/users/{userId}")
    @Operation(summary = "Thu hồi quyền của user")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<Void>> revokePermission(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID workspaceId,
            @PathVariable UUID fileId,
            @PathVariable UUID userId
    ) {
        shareService.revokePermission(
                userDetails.getUsername(), workspaceId, fileId, userId
        );
        return ResponseEntity.ok(ApiResponse.success("Thu hồi quyền thành công"));
    }
}