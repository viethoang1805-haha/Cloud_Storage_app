package com.saas.cloud_storage_app.modules.member.service.impl;

import com.saas.cloud_storage_app.common.enums.WorkspaceRole;
import com.saas.cloud_storage_app.common.exception.AppException;
import com.saas.cloud_storage_app.common.exception.ErrorCode;
import com.saas.cloud_storage_app.modules.member.dto.request.MemberInviteRequest;
import com.saas.cloud_storage_app.modules.member.dto.request.UpdateMemberRoleRequest;
import com.saas.cloud_storage_app.modules.member.dto.response.MemberResponse;
import com.saas.cloud_storage_app.modules.member.entity.WorkspaceMember;
import com.saas.cloud_storage_app.modules.member.mapper.MemberMapper;
import com.saas.cloud_storage_app.modules.member.repository.WorkspaceMemberRepository;
import com.saas.cloud_storage_app.modules.member.service.WorkspaceMemberService;
import com.saas.cloud_storage_app.modules.user.entity.User;
import com.saas.cloud_storage_app.modules.user.service.UserService;
import com.saas.cloud_storage_app.modules.workspace.entity.Workspace;
import com.saas.cloud_storage_app.modules.workspace.service.WorkspaceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkspaceMemberServiceImpl implements WorkspaceMemberService {

    private final WorkspaceMemberRepository memberRepository;
    private final WorkspaceService workspaceService;
    private final UserService userService;
    private final MemberMapper memberMapper;

    // =============================================
    // MỜI THÀNH VIÊN
    // =============================================
    @Override
    @Transactional
    public MemberResponse inviteMember(
            String email,
            UUID workspaceId,
            MemberInviteRequest request) {

        User inviter = userService.getUserByEmail(email);
        Workspace workspace = workspaceService.findWorkspaceById(workspaceId);

        // (1) Kiểm tra người mời có quyền không
        WorkspaceMember inviterMember = memberRepository
                .findByWorkspaceIdAndUserId(workspaceId, inviter.getId())
                .orElseThrow(() -> new AppException(ErrorCode.WORKSPACE_ACCESS_DENIED));

        if (!inviterMember.canManageMembers()) {
            throw new AppException(
                    ErrorCode.WORKSPACE_ACCESS_DENIED,
                    "Chỉ OWNER và ADMIN mới có thể mời thành viên"
            );
        }

        // (2) Không cho mời với role OWNER
        if (request.getRole() == WorkspaceRole.OWNER) {
            throw new AppException(
                    ErrorCode.VALIDATION_FAILED,
                    "Không thể mời thành viên với role OWNER"
            );
        }

        // (3) Tìm user được mời theo email
        User invitee = userService.getUserByEmail(request.getEmail());

        // (4) Không mời chính mình
        if (invitee.getId().equals(inviter.getId())) {
            throw new AppException(
                    ErrorCode.VALIDATION_FAILED,
                    "Không thể mời chính mình vào workspace"
            );
        }

        // (5) Không mời người đã là member
        if (memberRepository.existsByWorkspaceIdAndUserId(
                workspaceId, invitee.getId())) {
            throw new AppException(ErrorCode.MEMBER_ALREADY_EXISTS);
        }

        // (6) Tạo membership mới
        WorkspaceMember newMember = WorkspaceMember.builder()
                .workspace(workspace)
                .user(invitee)
                .role(request.getRole())
                .invitedBy(inviter)
                .build();

        WorkspaceMember saved = memberRepository.save(newMember);

        log.info("Mời {} vào workspace '{}' bởi: {}",
                invitee.getEmail(), workspace.getName(), email);

        return memberMapper.toMemberResponse(saved);
    }

    // =============================================
    // DANH SÁCH THÀNH VIÊN
    // =============================================
    @Override
    @Transactional(readOnly = true)
    public List<MemberResponse> getMembers(String email, UUID workspaceId) {

        // (7) Chỉ member mới xem được danh sách
        workspaceService.validateMemberAccess(email, workspaceId);

        return memberRepository.findAllByWorkspaceId(workspaceId)
                .stream()
                .map(memberMapper::toMemberResponse)
                .toList();
    }

    // =============================================
    // ĐỔI ROLE THÀNH VIÊN
    // =============================================
    @Override
    @Transactional
    public MemberResponse updateMemberRole(
            String email,
            UUID workspaceId,
            UUID targetUserId,
            UpdateMemberRoleRequest request) {

        User requester = userService.getUserByEmail(email);

        // (8) Kiểm tra người request có quyền ADMIN trở lên
        WorkspaceMember requesterMember = memberRepository
                .findByWorkspaceIdAndUserId(workspaceId, requester.getId())
                .orElseThrow(() -> new AppException(ErrorCode.WORKSPACE_ACCESS_DENIED));

        if (!requesterMember.isAdminOrAbove()) {
            throw new AppException(
                    ErrorCode.WORKSPACE_ACCESS_DENIED,
                    "Chỉ OWNER và ADMIN mới có thể thay đổi role"
            );
        }

        // (9) Không cho đổi role của OWNER
        WorkspaceMember targetMember = memberRepository
                .findByWorkspaceIdAndUserId(workspaceId, targetUserId)
                .orElseThrow(() -> new AppException(ErrorCode.MEMBER_NOT_FOUND));

        if (targetMember.isOwner()) {
            throw new AppException(
                    ErrorCode.VALIDATION_FAILED,
                    "Không thể thay đổi role của OWNER"
            );
        }

        // (10) ADMIN không thể đổi role của ADMIN khác (chỉ OWNER mới được)
        if (targetMember.getRole() == WorkspaceRole.ADMIN
                && !requesterMember.isOwner()) {
            throw new AppException(
                    ErrorCode.WORKSPACE_ACCESS_DENIED,
                    "Chỉ OWNER mới có thể thay đổi role của ADMIN"
            );
        }

        // (11) Không cho đặt role OWNER
        if (request.getRole() == WorkspaceRole.OWNER) {
            throw new AppException(
                    ErrorCode.VALIDATION_FAILED,
                    "Không thể đặt role OWNER qua API này"
            );
        }

        memberRepository.updateMemberRole(
                workspaceId, targetUserId, request.getRole()
        );

        // (12) Reload để lấy data mới nhất
        WorkspaceMember updated = memberRepository
                .findByWorkspaceIdAndUserId(workspaceId, targetUserId)
                .orElseThrow();

        log.info("Đổi role {} → {} trong workspace {}",
                targetMember.getRole(), request.getRole(), workspaceId);

        return memberMapper.toMemberResponse(updated);
    }

    // =============================================
    // XÓA THÀNH VIÊN
    // =============================================
    @Override
    @Transactional
    public void removeMember(
            String email,
            UUID workspaceId,
            UUID targetUserId) {

        User requester = userService.getUserByEmail(email);

        WorkspaceMember requesterMember = memberRepository
                .findByWorkspaceIdAndUserId(workspaceId, requester.getId())
                .orElseThrow(() -> new AppException(ErrorCode.WORKSPACE_ACCESS_DENIED));

        // (13) Chỉ ADMIN trở lên mới được xóa member
        if (!requesterMember.isAdminOrAbove()) {
            throw new AppException(ErrorCode.WORKSPACE_ACCESS_DENIED);
        }

        WorkspaceMember targetMember = memberRepository
                .findByWorkspaceIdAndUserId(workspaceId, targetUserId)
                .orElseThrow(() -> new AppException(ErrorCode.MEMBER_NOT_FOUND));

        // (14) Không xóa được OWNER
        if (targetMember.isOwner()) {
            throw new AppException(
                    ErrorCode.VALIDATION_FAILED,
                    "Không thể xóa OWNER khỏi workspace"
            );
        }

        // (15) ADMIN không thể xóa ADMIN khác
        if (targetMember.getRole() == WorkspaceRole.ADMIN
                && !requesterMember.isOwner()) {
            throw new AppException(
                    ErrorCode.WORKSPACE_ACCESS_DENIED,
                    "Chỉ OWNER mới có thể xóa ADMIN"
            );
        }

        memberRepository.deleteByWorkspaceIdAndUserId(workspaceId, targetUserId);

        log.info("Xóa member {} khỏi workspace {} bởi: {}",
                targetUserId, workspaceId, email);
    }

    // =============================================
    // TỰ RỜI WORKSPACE
    // =============================================
    @Override
    @Transactional
    public void leaveWorkspace(String email, UUID workspaceId) {
        User user = userService.getUserByEmail(email);

        WorkspaceMember member = memberRepository
                .findByWorkspaceIdAndUserId(workspaceId, user.getId())
                .orElseThrow(() -> new AppException(ErrorCode.MEMBER_NOT_FOUND));

        // (16) OWNER không thể tự rời — phải chuyển quyền trước
        if (member.isOwner()) {
            throw new AppException(
                    ErrorCode.VALIDATION_FAILED,
                    "OWNER không thể rời workspace. Hãy chuyển quyền OWNER cho người khác trước"
            );
        }

        memberRepository.deleteByWorkspaceIdAndUserId(workspaceId, user.getId());

        log.info("User {} rời workspace {}", email, workspaceId);
    }
}