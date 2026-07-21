package com.saas.cloud_storage_app.modules.workspace.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WorkspaceUpdateRequest {

    @NotBlank(message = "Tên workspace không được để trống")
    @Size(min = 1, max = 255)
    private String name;

    @Size(max = 1000)
    private String description;
}