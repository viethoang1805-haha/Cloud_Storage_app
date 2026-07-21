package com.saas.cloud_storage_app.modules.member.dto.request;

import com.saas.cloud_storage_app.common.enums.WorkspaceRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MemberInviteRequest {

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không đúng định dạng")
    private String email; // (1) mời qua email thay vì userId

    @NotNull(message = "Role không được để trống")
    private WorkspaceRole role; // (2) ADMIN, MEMBER, hoặc VIEWER

    // (3) Validation bổ sung ở Service:
    // - Không mời OWNER (chỉ có 1 OWNER duy nhất)
    // - Không mời chính mình
    // - Không mời người đã là member
}