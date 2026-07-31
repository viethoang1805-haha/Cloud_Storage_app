package com.saas.cloud_storage_app.modules.notification.event;

import com.saas.cloud_storage_app.modules.user.entity.User;
import com.saas.cloud_storage_app.modules.workspace.entity.Workspace;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class MemberInvitedEvent extends ApplicationEvent {

    private final Workspace workspace;
    private final User invitee;   // người được mời
    private final User inviter;   // người mời

    public MemberInvitedEvent(
            Object source,
            Workspace workspace,
            User invitee,
            User inviter) {
        super(source);
        this.workspace = workspace;
        this.invitee = invitee;
        this.inviter = inviter;
    }
}