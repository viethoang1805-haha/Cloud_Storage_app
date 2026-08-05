package com.saas.cloud_storage_app.modules.folder.service.impl;

import com.saas.cloud_storage_app.common.exception.AppException;
import com.saas.cloud_storage_app.common.exception.ErrorCode;
import com.saas.cloud_storage_app.modules.folder.dto.request.FolderCreateRequest;
import com.saas.cloud_storage_app.modules.folder.dto.request.FolderMoveRequest;
import com.saas.cloud_storage_app.modules.folder.dto.request.FolderRenameRequest;
import com.saas.cloud_storage_app.modules.folder.dto.response.FolderResponse;
import com.saas.cloud_storage_app.modules.folder.dto.response.FolderTreeResponse;
import com.saas.cloud_storage_app.modules.folder.entity.Folder;
import com.saas.cloud_storage_app.modules.folder.mapper.FolderMapper;
import com.saas.cloud_storage_app.modules.folder.repository.FolderRepository;
import com.saas.cloud_storage_app.modules.folder.service.FolderService;
import com.saas.cloud_storage_app.modules.user.entity.User;
import com.saas.cloud_storage_app.modules.user.service.UserService;
import com.saas.cloud_storage_app.modules.workspace.entity.Workspace;
import com.saas.cloud_storage_app.modules.workspace.service.WorkspaceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FolderServiceImpl implements FolderService {

    private final FolderRepository folderRepository;
    private final WorkspaceService workspaceService;
    private final UserService userService;
    private final FolderMapper folderMapper;

    // =============================================
    // TẠO FOLDER
    // =============================================
    @Override
    @Transactional
    public FolderResponse createFolder(
            String email,
            UUID workspaceId,
            FolderCreateRequest request) {

        // (1) Kiểm tra user có quyền truy cập workspace không
        workspaceService.validateMemberAccess(email, workspaceId);

        User creator = userService.getUserByEmail(email);
        Workspace workspace = workspaceService.findWorkspaceById(workspaceId);

        // (2) Xử lý folder cha
        Folder parentFolder = null;
        if (request.getParentId() != null) {
            parentFolder = findFolderById(request.getParentId());

            // (3) Đảm bảo folder cha thuộc cùng workspace
            if (!parentFolder.getWorkspace().getId().equals(workspaceId)) {
                throw new AppException(
                        ErrorCode.FOLDER_NOT_FOUND,
                        "Folder cha không thuộc workspace này"
                );
            }
        }

        // (4) Kiểm tra tên folder có bị trùng trong cùng thư mục không
        checkDuplicateName(workspaceId, request.getParentId(), request.getName());

        // (5) Tạo folder mới
        Folder folder = Folder.builder()
                .name(request.getName().trim())
                .workspace(workspace)
                .parent(parentFolder)
                .createdByUser(creator)
                .build();

        Folder saved = folderRepository.save(folder);

        log.info("Tạo folder '{}' trong workspace {} bởi: {}",
                saved.getName(), workspaceId, email);

        return folderMapper.toFolderResponse(saved, 0L);
    }

    // =============================================
    // DANH SÁCH FOLDER GỐC
    // =============================================
    @Override
    @Transactional(readOnly = true)
    public List<FolderResponse> getRootFolders(String email, UUID workspaceId) {
        workspaceService.validateMemberAccess(email, workspaceId);

        // (6) Chỉ lấy folder gốc (parent = null)
        List<Folder> rootFolders = folderRepository
                .findAllByWorkspaceIdAndParentIsNullAndIsDeletedFalse(workspaceId);

        return rootFolders.stream()
                .map(folder -> {
                    long childCount = folderRepository
                            .countByParentIdAndIsDeletedFalse(folder.getId());
                    return folderMapper.toFolderResponse(folder, childCount);
                })
                .toList();
    }

    // =============================================
    // CHI TIẾT FOLDER
    // =============================================
    @Override
    @Transactional(readOnly = true)
    public FolderResponse getFolderById(
            String email,
            UUID workspaceId,
            UUID folderId) {

        workspaceService.validateMemberAccess(email, workspaceId);
        Folder folder = findFolderInWorkspace(folderId, workspaceId);

        long childCount = folderRepository
                .countByParentIdAndIsDeletedFalse(folderId);

        return folderMapper.toFolderResponse(folder, childCount);
    }

    // =============================================
    // LẤY CÂY THƯ MỤC
    // =============================================
    @Override
    @Transactional(readOnly = true)
    public FolderTreeResponse getFolderTree(
            String email,
            UUID workspaceId,
            UUID folderId) {

        workspaceService.validateMemberAccess(email, workspaceId);

        // (7) Load folder với toàn bộ cây con
        // CẢNH BÁO: Với cây sâu và rộng sẽ tốn nhiều memory
        // Trong production nên giới hạn depth hoặc dùng lazy loading
        Folder folder = folderRepository.findByIdAndIsDeletedFalse(folderId)
                .orElseThrow(() -> new AppException(ErrorCode.FOLDER_NOT_FOUND));

        return folderMapper.toFolderTreeResponse(folder, 0);
    }

    // =============================================
    // ĐỔI TÊN FOLDER
    // =============================================
    @Override
    @Transactional
    public FolderResponse renameFolder(
            String email,
            UUID workspaceId,
            UUID folderId,
            FolderRenameRequest request) {

        workspaceService.validateMemberAccess(email, workspaceId);
        Folder folder = findFolderInWorkspace(folderId, workspaceId);

        String newName = request.getName().trim();

        // (8) Kiểm tra tên mới có trùng không (bỏ qua chính nó)
        if (!folder.getName().equals(newName)) {
            checkDuplicateName(
                    workspaceId,
                    folder.getParent() != null
                            ? folder.getParent().getId()
                            : null,
                    newName
            );
        }

        folder.setName(newName);
        Folder saved = folderRepository.save(folder);

        long childCount = folderRepository
                .countByParentIdAndIsDeletedFalse(folderId);

        log.info("Đổi tên folder thành '{}' bởi: {}", newName, email);

        return folderMapper.toFolderResponse(saved, childCount);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FolderResponse> getChildFolders(
            String email, UUID workspaceId, UUID parentId) {
        workspaceService.validateMemberAccess(email, workspaceId);

        List<Folder> children = folderRepository
                .findAllByParentIdAndIsDeletedFalse(parentId);

        return children.stream()
                .map(folder -> {
                    long childCount = folderRepository
                            .countByParentIdAndIsDeletedFalse(folder.getId());
                    return folderMapper.toFolderResponse(folder, childCount);
                })
                .toList();
    }

    // =============================================
    // DI CHUYỂN FOLDER
    // =============================================
    @Override
    @Transactional
    public FolderResponse moveFolder(
            String email,
            UUID workspaceId,
            UUID folderId,
            FolderMoveRequest request) {

        workspaceService.validateMemberAccess(email, workspaceId);
        Folder folder = findFolderInWorkspace(folderId, workspaceId);

        Folder targetParent = null;

        if (request.getTargetParentId() != null) {
            targetParent = findFolderInWorkspace(
                    request.getTargetParentId(), workspaceId
            );

            // (9) Không di chuyển folder vào chính nó
            if (targetParent.getId().equals(folder.getId())) {
                throw new AppException(
                        ErrorCode.VALIDATION_FAILED,
                        "Không thể di chuyển folder vào chính nó"
                );
            }

            // (10) Không di chuyển folder vào folder con của nó
            // (tạo vòng lặp vô tận trong cây)
            if (isDescendant(folder, targetParent)) {
                throw new AppException(
                        ErrorCode.VALIDATION_FAILED,
                        "Không thể di chuyển folder vào folder con của nó"
                );
            }
        }

        // (11) Kiểm tra tên trùng trong folder đích
        checkDuplicateName(
                workspaceId,
                targetParent != null ? targetParent.getId() : null,
                folder.getName()
        );

        folder.setParent(targetParent);
        Folder saved = folderRepository.save(folder);

        long childCount = folderRepository
                .countByParentIdAndIsDeletedFalse(folderId);

        log.info("Di chuyển folder '{}' đến: {}",
                folder.getName(),
                targetParent != null ? targetParent.getName() : "root");

        return folderMapper.toFolderResponse(saved, childCount);
    }

    // =============================================
    // XÓA FOLDER (SOFT DELETE)
    // =============================================
    @Override
    @Transactional
    public void deleteFolder(
            String email,
            UUID workspaceId,
            UUID folderId) {

        workspaceService.validateMemberAccess(email, workspaceId);
        Folder folder = findFolderInWorkspace(folderId, workspaceId);

        // (12) Dùng recursive CTE để soft delete cả cây
        folderRepository.softDeleteWithChildren(folderId);

        log.info("Soft delete folder '{}' và toàn bộ con cháu bởi: {}",
                folder.getName(), email);
    }

    // =============================================
    // INTERNAL METHOD
    // =============================================
    @Override
    @Transactional(readOnly = true)
    public Folder findFolderById(UUID folderId) {
        return folderRepository.findByIdAndIsDeletedFalse(folderId)
                .orElseThrow(() -> new AppException(ErrorCode.FOLDER_NOT_FOUND));
    }

    // =============================================
    // PRIVATE HELPERS
    // =============================================

    // (13) Tìm folder và validate thuộc đúng workspace
    private Folder findFolderInWorkspace(UUID folderId, UUID workspaceId) {
        Folder folder = findFolderById(folderId);

        if (!folder.getWorkspace().getId().equals(workspaceId)) {
            throw new AppException(ErrorCode.FOLDER_NOT_FOUND);
        }

        return folder;
    }

    // (14) Kiểm tra tên folder có trùng trong cùng thư mục không
    private void checkDuplicateName(
            UUID workspaceId,
            UUID parentId,
            String name) {

        boolean isDuplicate;

        if (parentId == null) {
            // Folder gốc: kiểm tra trong workspace, không có cha
            isDuplicate = folderRepository
                    .findRootFolderByName(workspaceId, name)
                    .isPresent();
        } else {
            // Folder con: kiểm tra trong folder cha
            isDuplicate = folderRepository
                    .findByWorkspaceIdAndParentIdAndNameAndIsDeletedFalse(
                            workspaceId, parentId, name
                    )
                    .isPresent();
        }

        if (isDuplicate) {
            throw new AppException(
                    ErrorCode.VALIDATION_FAILED,
                    "Đã tồn tại folder tên '" + name + "' trong thư mục này"
            );
        }
    }

    // (15) Kiểm tra targetFolder có phải là con/cháu của sourceFolder không
    // Dùng để ngăn chặn circular reference khi di chuyển folder
    private boolean isDescendant(Folder source, Folder target) {
        // Traverse từ target lên cha cho đến root
        Folder current = target.getParent();

        while (current != null) {
            if (current.getId().equals(source.getId())) {
                // Tìm thấy source trong ancestors của target
                // → target là con/cháu của source
                return true;
            }
            current = current.getParent();
        }


        return false;
        // (16) Chú ý: method này có thể gây N+1 query nếu parent LAZY
        // Mỗi lần gọi current.getParent() có thể trigger 1 query
        // Với cây nông (<10 level) thì chấp nhận được
        // Giải pháp tốt hơn: dùng path materialization hoặc native recursive query
    }
}