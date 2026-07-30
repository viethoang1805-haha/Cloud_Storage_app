package com.saas.cloud_storage_app.modules.file.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class FilePageResponse {

    private List<FileResponse> files;
    private int currentPage;
    private int totalPages;
    private long totalElements;  // tổng số file
    private int pageSize;
    private boolean hasNext;
    private boolean hasPrevious;
}