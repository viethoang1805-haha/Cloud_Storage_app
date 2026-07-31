package com.saas.cloud_storage_app.modules.notification.event;

import com.saas.cloud_storage_app.modules.file.entity.FileEntity;
import com.saas.cloud_storage_app.modules.user.entity.User;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.List;
import java.util.UUID;

@Getter
public class FileUploadedEvent extends ApplicationEvent {

    private final FileEntity file;
    private final User uploader;
    private final UUID workspaceId;
    // (1) Danh sách user cần nhận notification
    // (tất cả member của workspace, trừ người upload)
    private final List<User> recipients;

    public FileUploadedEvent(
            Object source,
            FileEntity file,
            User uploader,
            UUID workspaceId,
            List<User> recipients) {
        super(source);
        this.file = file;
        this.uploader = uploader;
        this.workspaceId = workspaceId;
        this.recipients = recipients;
    }
}