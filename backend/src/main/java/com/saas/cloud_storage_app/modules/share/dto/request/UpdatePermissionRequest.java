package com.saas.cloud_storage_app.modules.share.dto.request;

import com.saas.cloud_storage_app.common.enums.PermissionType;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdatePermissionRequest {

    @NotNull(message = "Permission không được để trống")
    private PermissionType permission;
}