package com.saas.cloud_storage_app.modules.workspace.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WorkspaceCreateRequest {

    @NotBlank(message = "Tên workspace không được để trống")
    @Size(min = 1, max = 255, message = "Tên workspace phải từ 1 đến 255 ký tự")
    private String name;

    @Size(max = 1000, message = "Mô tả không được vượt quá 1000 ký tự")
    private String description; // nullable — không bắt buộc
}