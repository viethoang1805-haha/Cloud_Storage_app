package com.saas.cloud_storage_app.modules.file.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FileSearchRequest {

    private String keyword;
    private String contentType;
    private String sortBy;
    private String sortDir;

    private int page = 0;

    private int size = 20;         // 20 file mỗi trang
}