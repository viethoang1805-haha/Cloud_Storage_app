-- =============================================
-- BẢNG workspaces
-- =============================================
CREATE TABLE workspaces (
                            id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                            name         VARCHAR(255) NOT NULL,
                            description  TEXT,                        -- (1) mô tả dài, dùng TEXT thay VARCHAR
                            owner_id     UUID NOT NULL,               -- (2) người tạo workspace
                            is_personal  BOOLEAN NOT NULL DEFAULT FALSE, -- (3) workspace cá nhân hay nhóm
                            created_at   TIMESTAMP NOT NULL DEFAULT NOW(),
                            updated_at   TIMESTAMP NOT NULL DEFAULT NOW(),
                            created_by   VARCHAR(255),
                            updated_by   VARCHAR(255),
                            CONSTRAINT fk_workspace_owner
                                FOREIGN KEY (owner_id) REFERENCES users(id)
                                    ON DELETE CASCADE   -- (4) xóa user → xóa luôn workspace của họ
);

-- =============================================
-- BẢNG workspace_members
-- =============================================
CREATE TABLE workspace_members (
                                   id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                   workspace_id  UUID NOT NULL,
                                   user_id       UUID NOT NULL,
                                   role          VARCHAR(50) NOT NULL DEFAULT 'MEMBER', -- (5) OWNER/ADMIN/MEMBER/VIEWER
                                   joined_at     TIMESTAMP NOT NULL DEFAULT NOW(),
                                   invited_by    UUID,                       -- (6) ai mời thành viên này vào
                                   created_at    TIMESTAMP NOT NULL DEFAULT NOW(),
                                   updated_at    TIMESTAMP NOT NULL DEFAULT NOW(),
                                   created_by    VARCHAR(255),
                                   updated_by    VARCHAR(255),

    -- (7) Mỗi user chỉ xuất hiện 1 lần trong 1 workspace
                                   CONSTRAINT uq_workspace_member UNIQUE (workspace_id, user_id),

                                   CONSTRAINT fk_member_workspace
                                       FOREIGN KEY (workspace_id) REFERENCES workspaces(id)
                                           ON DELETE CASCADE,  -- xóa workspace → xóa luôn members
                                   CONSTRAINT fk_member_user
                                       FOREIGN KEY (user_id) REFERENCES users(id)
                                           ON DELETE CASCADE,  -- xóa user → xóa luôn membership
                                   CONSTRAINT fk_member_invited_by
                                       FOREIGN KEY (invited_by) REFERENCES users(id)
                                           ON DELETE SET NULL  -- (8) người mời bị xóa → set NULL, không xóa member
);

-- =============================================
-- INDEX
-- =============================================
-- (9) Query thường dùng: tìm tất cả workspace của 1 user
CREATE INDEX idx_workspaces_owner_id
    ON workspaces(owner_id);

-- (10) Query thường dùng: tìm tất cả member của 1 workspace
CREATE INDEX idx_workspace_members_workspace_id
    ON workspace_members(workspace_id);

-- (11) Query thường dùng: tìm tất cả workspace mà 1 user là member
CREATE INDEX idx_workspace_members_user_id
    ON workspace_members(user_id);