package com.saas.cloud_storage_app.modules.activity.entity;

import com.saas.cloud_storage_app.modules.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "activity_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActivityLog {
    // (1) Không extends BaseEntity — chỉ cần created_at, không cần updated_at

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    // (2) Actor — có thể null nếu user bị xóa
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_id")
    private User actor;

    // (3) Snapshot data — luôn có dù actor bị xóa
    @Column(name = "actor_email", nullable = false, length = 255)
    private String actorEmail;

    @Column(name = "actor_name", nullable = false, length = 255)
    private String actorName;

    @Column(name = "action", nullable = false, length = 100)
    private String action;  // "FILE_UPLOADED", "MEMBER_INVITED"...

    // (4) Workspace context
    @Column(name = "workspace_id")
    private UUID workspaceId;

    @Column(name = "workspace_name", length = 255)
    private String workspaceName;

    // (5) Target object
    @Column(name = "target_type", length = 50)
    private String targetType;  // "FILE", "FOLDER", "MEMBER", "WORKSPACE"

    @Column(name = "target_id")
    private UUID targetId;

    @Column(name = "target_name", length = 255)
    private String targetName;

    // (6) Flexible metadata dạng JSON
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", columnDefinition = "jsonb")
    private Map<String, Object> metadata;

    @Column(name = "ip_address", length = 50)
    private String ipAddress;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}