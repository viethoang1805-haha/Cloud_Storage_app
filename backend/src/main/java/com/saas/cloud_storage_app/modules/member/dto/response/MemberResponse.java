package com.saas.cloud_storage_app.modules.member.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class MemberResponse {

    private String id;          // workspace_member id
    private String role;        // "OWNER", "ADMIN", "MEMBER", "VIEWER"
    private LocalDateTime joinedAt;

    // (1) Thông tin user của member
    private MemberUserInfo user;

    // (2) Người mời — nullable
    private MemberUserInfo invitedBy;

    @Getter
    @Builder
    @AllArgsConstructor
    public static class MemberUserInfo {
        private String id;
        private String fullName;
        private String email;
        private String avatarUrl;
    }
}