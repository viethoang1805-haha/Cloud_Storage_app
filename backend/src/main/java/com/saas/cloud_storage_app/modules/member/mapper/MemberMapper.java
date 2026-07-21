package com.saas.cloud_storage_app.modules.member.mapper;

import com.saas.cloud_storage_app.modules.member.dto.response.MemberResponse;
import com.saas.cloud_storage_app.modules.member.entity.WorkspaceMember;
import com.saas.cloud_storage_app.modules.user.entity.User;
import org.springframework.stereotype.Component;

@Component
public class MemberMapper {

    public MemberResponse toMemberResponse(WorkspaceMember member) {
        return MemberResponse.builder()
                .id(member.getId().toString())
                .role(member.getRole().name())
                .joinedAt(member.getJoinedAt())
                .user(toMemberUserInfo(member.getUser()))
                // (1) invitedBy có thể null — dùng conditional mapping
                .invitedBy(
                        member.getInvitedBy() != null
                                ? toMemberUserInfo(member.getInvitedBy())
                                : null
                )
                .build();
    }

    private MemberResponse.MemberUserInfo toMemberUserInfo(User user) {
        return MemberResponse.MemberUserInfo.builder()
                .id(user.getId().toString())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .avatarUrl(user.getAvatarUrl())
                .build();
    }
}