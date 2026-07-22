package com.saas.cloud_storage_app.modules.folder.mapper;

import com.saas.cloud_storage_app.modules.folder.dto.response.FolderResponse;
import com.saas.cloud_storage_app.modules.folder.dto.response.FolderTreeResponse;
import com.saas.cloud_storage_app.modules.folder.entity.Folder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class FolderMapper {

    // (1) Folder entity → FolderResponse
    public FolderResponse toFolderResponse(Folder folder, long childCount) {
        return FolderResponse.builder()
                .id(folder.getId().toString())
                .name(folder.getName())
                .workspaceId(folder.getWorkspace().getId().toString())
                .parent(folder.getParent() != null
                        ? toParentInfo(folder.getParent())
                        : null  // folder gốc không có parent
                )
                .childCount(childCount)
                .isDeleted(folder.isDeleted())
                .createdAt(folder.getCreatedAt())
                .updatedAt(folder.getUpdatedAt())
                .createdByUser(folder.getCreatedByUser() != null
                        ? toCreatorInfo(folder)
                        : null
                )
                .build();
    }

    // (2) Build cây đệ quy từ folder entity
    public FolderTreeResponse toFolderTreeResponse(Folder folder, int depth) {
        // (3) Map danh sách folder con thành FolderTreeResponse
        // Mỗi folder con lại được map đệ quy với depth + 1
        List<FolderTreeResponse> childrenDto = folder.getChildren()
                .stream()
                .filter(child -> !child.isDeleted()) // bỏ qua folder đã xóa
                .map(child -> toFolderTreeResponse(child, depth + 1))
                .toList();

        return FolderTreeResponse.builder()
                .id(folder.getId().toString())
                .name(folder.getName())
                .depth(depth)
                .children(childrenDto)
                .childCount(childrenDto.size())
                .build();
    }

    private FolderResponse.ParentInfo toParentInfo(Folder parent) {
        return FolderResponse.ParentInfo.builder()
                .id(parent.getId().toString())
                .name(parent.getName())
                .build();
    }

    private FolderResponse.CreatorInfo toCreatorInfo(Folder folder) {
        var creator = folder.getCreatedByUser();
        return FolderResponse.CreatorInfo.builder()
                .id(creator.getId().toString())
                .fullName(creator.getFullName())
                .avatarUrl(creator.getAvatarUrl())
                .build();
    }
}