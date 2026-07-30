package com.saas.cloud_storage_app.modules.file.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class FileUploadRequest {

    // (1) Nullable — upload vào root workspace nếu không chỉ định folder
    private UUID folderId;
}