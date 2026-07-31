package com.saas.cloud_storage_app.modules.activity.builder;

import com.saas.cloud_storage_app.common.enums.ActivityType;
import com.saas.cloud_storage_app.modules.activity.entity.ActivityLog;
import com.saas.cloud_storage_app.modules.file.entity.FileEntity;
import com.saas.cloud_storage_app.modules.folder.entity.Folder;
import com.saas.cloud_storage_app.modules.user.entity.User;
import com.saas.cloud_storage_app.modules.workspace.entity.Workspace;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class ActivityLogBuilder {

    // =============================================
    // (1) Base builder method — tất cả method khác gọi cái này
    // =============================================
    public ActivityLog build(
            User actor,
            String action,
            Workspace workspace,
            String targetType,
            String targetId,
            String targetName,
            Map<String, Object> metadata,
            String ipAddress,
            String userAgent) {

        return ActivityLog.builder()
                .actor(actor)
                // (2) Snapshot: lưu email/name tại thời điểm này
                .actorEmail(actor.getEmail())
                .actorName(actor.getFullName())
                .action(action)
                .workspaceId(workspace != null ? workspace.getId() : null)
                .workspaceName(workspace != null ? workspace.getName() : null)
                .targetType(targetType)
                .targetId(targetId != null
                        ? java.util.UUID.fromString(targetId) : null)
                .targetName(targetName)
                .metadata(metadata)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .build();
    }

    // =============================================
    // FACTORY METHODS — tạo log cho từng loại action
    // =============================================

    // (3) File Upload
    public ActivityLog fileUploaded(
            User actor, Workspace workspace,
            FileEntity file, String ip, String ua) {

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("fileSize", file.getSize());
        metadata.put("contentType", file.getContentType());
        metadata.put("folderId",
                file.getFolder() != null
                        ? file.getFolder().getId().toString()
                        : null);

        return build(actor,
                ActivityType.FILE_UPLOADED.name(),
                workspace,
                "FILE",
                file.getId().toString(),
                file.getOriginalName(),
                metadata, ip, ua);
    }

    // File Delete
    public ActivityLog fileDeleted(
            User actor, Workspace workspace,
            FileEntity file, String ip, String ua) {

        return build(actor,
                ActivityType.FILE_DELETED.name(),
                workspace,
                "FILE",
                file.getId().toString(),
                file.getOriginalName(),
                null, ip, ua);
    }

    // File Download
    public ActivityLog fileDownloaded(
            User actor, Workspace workspace,
            FileEntity file, String via, String ip, String ua) {

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("via", via); // "direct" hoặc "share_link"

        return build(actor,
                ActivityType.FILE_DOWNLOADED.name(),
                workspace,
                "FILE",
                file.getId().toString(),
                file.getOriginalName(),
                metadata, ip, ua);
    }

    // File Shared
    public ActivityLog fileShared(
            User actor, Workspace workspace,
            FileEntity file, User sharedWith,
            String permission, String ip, String ua) {

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("sharedWithEmail", sharedWith.getEmail());
        metadata.put("sharedWithName", sharedWith.getFullName());
        metadata.put("permission", permission);

        return build(actor,
                ActivityType.FILE_SHARED.name(),
                workspace,
                "FILE",
                file.getId().toString(),
                file.getOriginalName(),
                metadata, ip, ua);
    }

    // Folder Created
    public ActivityLog folderCreated(
            User actor, Workspace workspace,
            Folder folder, String ip, String ua) {

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("parentFolderId",
                folder.getParent() != null
                        ? folder.getParent().getId().toString()
                        : null);

        return build(actor,
                ActivityType.FOLDER_CREATED.name(),
                workspace,
                "FOLDER",
                folder.getId().toString(),
                folder.getName(),
                metadata, ip, ua);
    }

    // Folder Deleted
    public ActivityLog folderDeleted(
            User actor, Workspace workspace,
            Folder folder, String ip, String ua) {

        return build(actor,
                ActivityType.FOLDER_DELETED.name(),
                workspace,
                "FOLDER",
                folder.getId().toString(),
                folder.getName(),
                null, ip, ua);
    }

    // Member Invited
    public ActivityLog memberInvited(
            User actor, Workspace workspace,
            User invitee, String role,
            String ip, String ua) {

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("inviteeEmail", invitee.getEmail());
        metadata.put("inviteeName", invitee.getFullName());
        metadata.put("role", role);

        return build(actor,
                ActivityType.MEMBER_INVITED.name(),
                workspace,
                "MEMBER",
                invitee.getId().toString(),
                invitee.getFullName(),
                metadata, ip, ua);
    }

    // Member Removed
    public ActivityLog memberRemoved(
            User actor, Workspace workspace,
            User removedUser, String ip, String ua) {

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("removedEmail", removedUser.getEmail());

        return build(actor,
                ActivityType.MEMBER_REMOVED.name(),
                workspace,
                "MEMBER",
                removedUser.getId().toString(),
                removedUser.getFullName(),
                metadata, ip, ua);
    }

    // Workspace Created
    public ActivityLog workspaceCreated(
            User actor, Workspace workspace,
            String ip, String ua) {

        return build(actor,
                ActivityType.WORKSPACE_CREATED.name(),
                workspace,
                "WORKSPACE",
                workspace.getId().toString(),
                workspace.getName(),
                null, ip, ua);
    }

    // User Login
    public ActivityLog userLogin(
            User actor, String ip, String ua) {

        return build(actor,
                ActivityType.USER_LOGIN.name(),
                null,   // (4) login không thuộc workspace cụ thể
                "USER",
                actor.getId().toString(),
                actor.getEmail(),
                null, ip, ua);
    }
}