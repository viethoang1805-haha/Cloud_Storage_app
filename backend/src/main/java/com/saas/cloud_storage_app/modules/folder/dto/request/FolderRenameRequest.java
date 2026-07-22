package com.saas.cloud_storage_app.modules.folder.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FolderRenameRequest {

    @NotBlank(message = "Tên folder không được để trống")
    @Size(min = 1, max = 255)
    @Pattern(
            regexp = "^[^/\\\\:*?\"<>|]+$",
            message = "Tên folder không được chứa ký tự đặc biệt"
    )
    private String name;
}