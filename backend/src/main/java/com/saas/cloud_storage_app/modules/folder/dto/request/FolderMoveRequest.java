package com.saas.cloud_storage_app.modules.folder.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class FolderMoveRequest {

    // (1) null = di chuyển lên folder gốc
    private UUID targetParentId;
}