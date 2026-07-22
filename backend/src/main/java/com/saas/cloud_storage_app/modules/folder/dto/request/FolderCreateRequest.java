package com.saas.cloud_storage_app.modules.folder.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class FolderCreateRequest {

    @NotBlank(message = "Tên folder không được để trống")
    @Size(min = 1, max = 255, message = "Tên folder phải từ 1 đến 255 ký tự")
    @Pattern(
            // (1) Không cho phép ký tự đặc biệt trong tên folder
            regexp = "^[^/\\\\:*?\"<>|]+$",
            message = "Tên folder không được chứa các ký tự: / \\ : * ? \" < > |"
    )
    private String name;

    // (2) nullable — null nghĩa là tạo folder gốc
    private UUID parentId;
}