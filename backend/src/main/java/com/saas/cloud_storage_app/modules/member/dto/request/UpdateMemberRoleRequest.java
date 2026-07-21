package com.saas.cloud_storage_app.modules.member.dto.request;

import com.saas.cloud_storage_app.common.enums.WorkspaceRole;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateMemberRoleRequest {

    @NotNull(message = "Role không được để trống")
    private WorkspaceRole role;
}