package com.saas.cloud_storage_app.modules.file.controller;

import com.saas.cloud_storage_app.common.response.ApiResponse;
import com.saas.cloud_storage_app.modules.file.dto.request.FileSearchRequest;
import com.saas.cloud_storage_app.modules.file.dto.response.DownloadUrlResponse;
import com.saas.cloud_storage_app.modules.file.dto.response.FilePageResponse;
import com.saas.cloud_storage_app.modules.file.dto.response.FileResponse;
import com.saas.cloud_storage_app.modules.file.service.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/workspaces/{workspaceId}/files")
@RequiredArgsConstructor
@Tag(name = "File", description = "API quản lý file")
@SecurityRequirement(name = "bearerAuth")
public class FileController {

    private final FileService fileService;

    // (1) Upload file — dùng multipart/form-data
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload file lên workspace")
    public ResponseEntity<ApiResponse<FileResponse>> uploadFile(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID workspaceId,
            @RequestParam(required = false) UUID folderId, // (2) form param
            @RequestPart("file") MultipartFile file        // (3) binary file part
    ) {
        FileResponse response = fileService.uploadFile(
                userDetails.getUsername(), workspaceId, folderId, file
        );
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response,"Upload thành công"));
    }

    // Lấy file trong folder
    @GetMapping("/folder/{folderId}")
    @Operation(summary = "Danh sách file trong folder")
    public ResponseEntity<ApiResponse<FilePageResponse>> getFilesInFolder(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID workspaceId,
            @PathVariable UUID folderId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        FilePageResponse response = fileService.getFilesInFolder(
                userDetails.getUsername(), workspaceId, folderId, page, size
        );
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // Lấy file ở root workspace
    @GetMapping("/root")
    @Operation(summary = "Danh sách file ở root workspace")
    public ResponseEntity<ApiResponse<FilePageResponse>> getFilesInRoot(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID workspaceId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        FilePageResponse response = fileService.getFilesInRoot(
                userDetails.getUsername(), workspaceId, page, size
        );
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // Tìm kiếm file
    @GetMapping("/search")
    @Operation(summary = "Tìm kiếm file theo tên")
    public ResponseEntity<ApiResponse<FilePageResponse>> searchFiles(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID workspaceId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        FileSearchRequest request = new FileSearchRequest();
        request.setKeyword(keyword);
        request.setPage(page);
        request.setSize(size);
        request.setSortBy(sortBy);
        request.setSortDir(sortDir);

        FilePageResponse response = fileService.searchFiles(
                userDetails.getUsername(), workspaceId, request
        );
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // Chi tiết file
    @GetMapping("/{fileId}")
    @Operation(summary = "Chi tiết file")
    public ResponseEntity<ApiResponse<FileResponse>> getFileById(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID workspaceId,
            @PathVariable UUID fileId
    ) {
        FileResponse response = fileService.getFileById(
                userDetails.getUsername(), workspaceId, fileId
        );
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // Lấy URL download
    @GetMapping("/{fileId}/download-url")
    @Operation(summary = "Lấy presigned URL để download file")
    public ResponseEntity<ApiResponse<DownloadUrlResponse>> getDownloadUrl(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID workspaceId,
            @PathVariable UUID fileId
    ) {
        DownloadUrlResponse response = fileService.getDownloadUrl(
                userDetails.getUsername(), workspaceId, fileId
        );
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // Di chuyển file
    @PatchMapping("/{fileId}/move")
    @Operation(summary = "Di chuyển file sang folder khác")
    public ResponseEntity<ApiResponse<FileResponse>> moveFile(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID workspaceId,
            @PathVariable UUID fileId,
            @RequestParam(required = false) UUID targetFolderId // (4) null = về root
    ) {
        FileResponse response = fileService.moveFile(
                userDetails.getUsername(), workspaceId, fileId, targetFolderId
        );
        return ResponseEntity.ok(ApiResponse.success(response,"Di chuyển thành công" ));
    }

    // Xóa file
    @DeleteMapping("/{fileId}")
    @Operation(summary = "Xóa file")
    public ResponseEntity<ApiResponse<Void>> deleteFile(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID workspaceId,
            @PathVariable UUID fileId
    ) {
        fileService.deleteFile(
                userDetails.getUsername(), workspaceId, fileId
        );
        return ResponseEntity.ok(ApiResponse.success("Xóa file thành công"));
    }
}