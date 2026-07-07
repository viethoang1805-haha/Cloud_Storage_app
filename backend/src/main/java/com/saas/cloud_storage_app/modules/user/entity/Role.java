package com.saas.cloud_storage_app.modules.user.entity;

import com.saas.cloud_storage_app.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role extends BaseEntity {

    @Column(name = "name", nullable = false, unique = true, length = 50)
    private String name; // "ROLE_USER" hoặc "ROLE_ADMIN"
}