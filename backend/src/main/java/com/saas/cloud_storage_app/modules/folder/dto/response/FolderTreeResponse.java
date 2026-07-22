package com.saas.cloud_storage_app.modules.folder.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class FolderTreeResponse {

    private String id;
    private String name;
    private int depth;   // (1) độ sâu trong cây (0 = root)

    // (2) Danh sách folder con — cũng là FolderTreeResponse (đệ quy)
    @Builder.Default
    private List<FolderTreeResponse> children = new ArrayList<>();

    private long childCount;
}