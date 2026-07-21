package com.saas.cloud_storage_app.modules.workspace.entity;

import com.saas.cloud_storage_app.common.entity.BaseEntity;
import com.saas.cloud_storage_app.modules.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "workspaces")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Workspace extends BaseEntity {

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")  // (1)
    private String description;

    // (2) Quan hệ nhiều-một: nhiều workspace thuộc về 1 user (owner)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Column(name = "is_personal", nullable = false)
    @Builder.Default
    private boolean isPersonal = false;
}