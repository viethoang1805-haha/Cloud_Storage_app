package com.saas.cloud_storage_app.modules.notification.event;

import com.saas.cloud_storage_app.common.enums.PermissionType;
import com.saas.cloud_storage_app.modules.file.entity.FileEntity;
import com.saas.cloud_storage_app.modules.user.entity.User;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class FileSharedEvent extends ApplicationEvent {

    private final FileEntity file;
    private final User sharedWith;   // người nhận
    private final User sharedBy;     // người chia sẻ
    private final PermissionType permission;

    public FileSharedEvent(
            Object source,
            FileEntity file,
            User sharedWith,
            User sharedBy,
            PermissionType permission) {
        super(source);
        this.file = file;
        this.sharedWith = sharedWith;
        this.sharedBy = sharedBy;
        this.permission = permission;
    }
}