package com.saas.cloud_storage_app.modules.member.service;

import com.saas.cloud_storage_app.modules.member.dto.request.MemberInviteRequest;
import com.saas.cloud_storage_app.modules.member.dto.request.UpdateMemberRoleRequest;
import com.saas.cloud_storage_app.modules.member.dto.response.MemberResponse;

import java.util.List;
import java.util.UUID;

public interface WorkspaceMemberService {
    MemberResponse inviteMember(String email, UUID workspaceId, MemberInviteRequest request);
    List<MemberResponse> getMembers(String email, UUID workspaceId);
    MemberResponse updateMemberRole(String email, UUID workspaceId, UUID targetUserId, UpdateMemberRoleRequest request);
    void removeMember(String email, UUID workspaceId, UUID targetUserId);
    void leaveWorkspace(String email, UUID workspaceId);
}