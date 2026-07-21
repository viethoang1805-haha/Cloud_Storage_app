package com.saas.cloud_storage_app.modules.workspace.mapper;

import com.saas.cloud_storage_app.modules.member.entity.WorkspaceMember;
import com.saas.cloud_storage_app.modules.workspace.dto.response.WorkspaceResponse;
import com.saas.cloud_storage_app.modules.workspace.entity.Workspace;
import org.springframework.stereotype.Component;

@Component
public class WorkspaceMapper {

    // (1) Chuyển Workspace → WorkspaceResponse
    // Cần thêm myRole và memberCount vì không lấy được từ entity đơn thuần
    public WorkspaceResponse toWorkspaceResponse(
            Workspace workspace,
            String myRole,       // role của user đang request
            long memberCount     // tổng số member
    ) {
        return WorkspaceResponse.builder()
                .id(workspace.getId().toString())
                .name(workspace.getName())
                .description(workspace.getDescription())
                .isPersonal(workspace.isPersonal())
                .owner(toOwnerInfo(workspace))
                .myRole(myRole)
                .memberCount(memberCount)
                .createdAt(workspace.getCreatedAt())
                .build();
    }

    // (2) Helper — chuyển Workspace owner → OwnerInfo
    private WorkspaceResponse.OwnerInfo toOwnerInfo(Workspace workspace) {
        var owner = workspace.getOwner();
        return WorkspaceResponse.OwnerInfo.builder()
                .id(owner.getId().toString())
                .fullName(owner.getFullName())
                .email(owner.getEmail())
                .avatarUrl(owner.getAvatarUrl())
                .build();
    }
}