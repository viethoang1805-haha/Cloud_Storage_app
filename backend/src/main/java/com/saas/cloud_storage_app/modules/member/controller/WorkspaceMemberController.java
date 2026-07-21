package com.saas.cloud_storage_app.modules.member.controller;

import com.saas.cloud_storage_app.common.response.ApiResponse;
import com.saas.cloud_storage_app.modules.member.dto.request.MemberInviteRequest;
import com.saas.cloud_storage_app.modules.member.dto.request.UpdateMemberRoleRequest;
import com.saas.cloud_storage_app.modules.member.dto.response.MemberResponse;
import com.saas.cloud_storage_app.modules.member.service.WorkspaceMemberService;
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
@RequestMapping("/api/v1/workspaces/{workspaceId}/members")  // (2)
@RequiredArgsConstructor
@Tag(name = "Workspace Member", description = "API quản lý thành viên workspace")
@SecurityRequirement(name = "bearerAuth")
public class WorkspaceMemberController {

    private final WorkspaceMemberService memberService;

    @PostMapping
    @Operation(summary = "Mời thành viên vào workspace")
    public ResponseEntity<ApiResponse<MemberResponse>> inviteMember(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID workspaceId,
            @Valid @RequestBody MemberInviteRequest request
    ) {
        MemberResponse response = memberService
                .inviteMember(userDetails.getUsername(), workspaceId, request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response,"Mời thành viên thành công" ));
    }

    @GetMapping
    @Operation(summary = "Danh sách thành viên workspace")
    public ResponseEntity<ApiResponse<List<MemberResponse>>> getMembers(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID workspaceId
    ) {
        List<MemberResponse> response = memberService
                .getMembers(userDetails.getUsername(), workspaceId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{userId}")
    @Operation(summary = "Đổi role thành viên")
    public ResponseEntity<ApiResponse<MemberResponse>> updateMemberRole(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID workspaceId,
            @PathVariable UUID userId,
            @Valid @RequestBody UpdateMemberRoleRequest request
    ) {
        MemberResponse response = memberService
                .updateMemberRole(
                        userDetails.getUsername(),
                        workspaceId,
                        userId,
                        request
                );
        return ResponseEntity.ok(ApiResponse.success(response,"Cập nhật role thành công" ));
    }

    @DeleteMapping("/{userId}")
    @Operation(summary = "Xóa thành viên khỏi workspace")
    public ResponseEntity<ApiResponse<Void>> removeMember(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID workspaceId,
            @PathVariable UUID userId
    ) {
        memberService.removeMember(userDetails.getUsername(), workspaceId, userId);
        return ResponseEntity.ok(ApiResponse.success("Xóa thành viên thành công"));
    }

    @DeleteMapping("/me")  // (3)
    @Operation(summary = "Tự rời khỏi workspace")
    public ResponseEntity<ApiResponse<Void>> leaveWorkspace(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID workspaceId
    ) {
        memberService.leaveWorkspace(userDetails.getUsername(), workspaceId);
        return ResponseEntity.ok(ApiResponse.success("Rời workspace thành công"));
    }
}