package com.saas.cloud_storage_app.modules.workspace.service.impl;

import com.saas.cloud_storage_app.common.enums.WorkspaceRole;
import com.saas.cloud_storage_app.common.exception.AppException;
import com.saas.cloud_storage_app.common.exception.ErrorCode;
import com.saas.cloud_storage_app.modules.member.entity.WorkspaceMember;
import com.saas.cloud_storage_app.modules.member.repository.WorkspaceMemberRepository;
import com.saas.cloud_storage_app.modules.user.entity.User;
import com.saas.cloud_storage_app.modules.user.service.UserService;
import com.saas.cloud_storage_app.modules.workspace.dto.request.WorkspaceCreateRequest;
import com.saas.cloud_storage_app.modules.workspace.dto.request.WorkspaceUpdateRequest;
import com.saas.cloud_storage_app.modules.workspace.dto.response.WorkspaceResponse;
import com.saas.cloud_storage_app.modules.workspace.entity.Workspace;
import com.saas.cloud_storage_app.modules.workspace.mapper.WorkspaceMapper;
import com.saas.cloud_storage_app.modules.workspace.repository.WorkspaceRepository;
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
public class WorkspaceServiceImpl implements WorkspaceService {

    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMemberRepository memberRepository;
    private final UserService userService;
    private final WorkspaceMapper workspaceMapper;

    // =============================================
    // TẠO WORKSPACE
    // =============================================
    @Override
    @Transactional
    public WorkspaceResponse createWorkspace(String email, WorkspaceCreateRequest request) {
        User owner = userService.getUserByEmail(email);

        Workspace workspace = Workspace.builder()
                .name(request.getName().trim())
                .description(request.getDescription())
                .owner(owner)
                .isPersonal(false)
                .build();

        Workspace saved = workspaceRepository.save(workspace);

        WorkspaceMember ownerMember = WorkspaceMember.builder()
                .workspace(saved)
                .user(owner)
                .role(WorkspaceRole.OWNER)
                .invitedBy(null)
                .build();

        memberRepository.save(ownerMember);
        log.info("Tạo workspace '{}' bởi: {}", saved.getName(), email);

        return workspaceMapper.toWorkspaceResponse(saved, WorkspaceRole.OWNER.name(), 1L);
    }

    // =============================================
    // DANH SÁCH WORKSPACE CỦA TÔI
    // =============================================
    @Override
    @Transactional(readOnly = true)
    public List<WorkspaceResponse> getMyWorkspaces(String email) {
        User user = userService.getUserByEmail(email);

        List<Workspace> workspaces = workspaceRepository.findAllByMemberId(user.getId());

        return workspaces.stream()
                .map(workspace -> {
                    WorkspaceMember member = memberRepository
                            .findByWorkspaceIdAndUserId(workspace.getId(), user.getId())
                            .orElseThrow();

                    long memberCount = memberRepository.countByWorkspaceId(workspace.getId());

                    return workspaceMapper.toWorkspaceResponse(
                            workspace, member.getRole().name(), memberCount);
                })
                .toList();
    }

    // =============================================
    // CHI TIẾT WORKSPACE
    // =============================================
    @Override
    @Transactional(readOnly = true)
    public WorkspaceResponse getWorkspaceById(String email, UUID workspaceId) {
        User user = userService.getUserByEmail(email);
        Workspace workspace = findWorkspaceById(workspaceId);

        WorkspaceMember member = memberRepository
                .findByWorkspaceIdAndUserId(workspaceId, user.getId())
                .orElseThrow(() -> new AppException(ErrorCode.WORKSPACE_ACCESS_DENIED));

        long memberCount = memberRepository.countByWorkspaceId(workspaceId);

        return workspaceMapper.toWorkspaceResponse(
                workspace, member.getRole().name(), memberCount);
    }

    // =============================================
    // CẬP NHẬT WORKSPACE
    // =============================================
    @Override
    @Transactional
    public WorkspaceResponse updateWorkspace(
            String email, UUID workspaceId, WorkspaceUpdateRequest request) {

        User user = userService.getUserByEmail(email);
        Workspace workspace = findWorkspaceById(workspaceId);

        WorkspaceMember member = getMemberOrThrow(workspaceId, user.getId());
        if (!member.isAdminOrAbove()) {
            throw new AppException(ErrorCode.WORKSPACE_ACCESS_DENIED);
        }

        workspace.setName(request.getName().trim());
        workspace.setDescription(request.getDescription());
        Workspace saved = workspaceRepository.save(workspace);

        long memberCount = memberRepository.countByWorkspaceId(workspaceId);
        log.info("Cập nhật workspace '{}' bởi: {}", saved.getName(), email);

        return workspaceMapper.toWorkspaceResponse(
                saved, member.getRole().name(), memberCount);
    }

    // =============================================
    // XÓA WORKSPACE
    // =============================================
    @Override
    @Transactional
    public void deleteWorkspace(String email, UUID workspaceId) {
        User user = userService.getUserByEmail(email);
        Workspace workspace = findWorkspaceById(workspaceId);

        if (!workspace.getOwner().getId().equals(user.getId())) {
            throw new AppException(ErrorCode.WORKSPACE_ACCESS_DENIED);
        }

        if (workspace.isPersonal()) {
            throw new AppException(ErrorCode.VALIDATION_FAILED, "Không thể xóa workspace cá nhân");
        }

        workspaceRepository.delete(workspace);
        log.info("Xóa workspace '{}' bởi: {}", workspace.getName(), email);
    }

    // =============================================
    // INTERNAL METHODS
    // =============================================
    @Override
    @Transactional(readOnly = true)
    public Workspace findWorkspaceById(UUID workspaceId) {
        return workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new AppException(ErrorCode.WORKSPACE_NOT_FOUND));
    }

    @Override
    public void validateMemberAccess(String email, UUID workspaceId) {
        User user = userService.getUserByEmail(email);
        boolean isMember = memberRepository
                .existsByWorkspaceIdAndUserId(workspaceId, user.getId());
        if (!isMember) {
            throw new AppException(ErrorCode.WORKSPACE_ACCESS_DENIED);
        }
    }

    @Override
    public void validateAdminAccess(String email, UUID workspaceId) {
        User user = userService.getUserByEmail(email);
        WorkspaceMember member = getMemberOrThrow(workspaceId, user.getId());
        if (!member.isAdminOrAbove()) {
            throw new AppException(ErrorCode.WORKSPACE_ACCESS_DENIED);
        }
    }

    // =============================================
    // PRIVATE HELPERS
    // =============================================
    private WorkspaceMember getMemberOrThrow(UUID workspaceId, UUID userId) {
        return memberRepository
                .findByWorkspaceIdAndUserId(workspaceId, userId)
                .orElseThrow(() -> new AppException(ErrorCode.WORKSPACE_ACCESS_DENIED));
    }
}