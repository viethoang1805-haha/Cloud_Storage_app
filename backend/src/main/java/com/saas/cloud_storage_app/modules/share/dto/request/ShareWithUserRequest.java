package com.saas.cloud_storage_app.modules.share.dto.request;

import com.saas.cloud_storage_app.common.enums.PermissionType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ShareWithUserRequest {

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không đúng định dạng")
    private String email;

    @NotNull(message = "Permission không được để trống")
    private PermissionType permission;

    @Future(message = "Thời gian hết hạn phải trong tương lai")
    private LocalDateTime expiresAt; // null = không hết hạn
}