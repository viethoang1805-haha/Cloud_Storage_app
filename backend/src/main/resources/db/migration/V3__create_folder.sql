CREATE TABLE folders (
                         id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                         name          VARCHAR(255) NOT NULL,
                         workspace_id  UUID NOT NULL,
                         parent_id     UUID,         -- (1) NULL = folder gốc, có giá trị = folder con
                         created_by_id UUID NOT NULL, -- (2) user tạo folder
                         is_deleted    BOOLEAN NOT NULL DEFAULT FALSE, -- (3) soft delete
                         deleted_at    TIMESTAMP,
                         created_at    TIMESTAMP NOT NULL DEFAULT NOW(),
                         updated_at    TIMESTAMP NOT NULL DEFAULT NOW(),
                         created_by    VARCHAR(255),
                         updated_by    VARCHAR(255),

                         CONSTRAINT fk_folder_workspace
                             FOREIGN KEY (workspace_id) REFERENCES workspaces(id)
                                 ON DELETE CASCADE,

    -- (4) Self-referencing FK: parent_id trỏ về chính bảng folders
                         CONSTRAINT fk_folder_parent
                             FOREIGN KEY (parent_id) REFERENCES folders(id)
                                 ON DELETE CASCADE, -- xóa folder cha → tự xóa folder con đệ quy

                         CONSTRAINT fk_folder_creator
                             FOREIGN KEY (created_by_id) REFERENCES users(id)
                                 ON DELETE SET NULL,

    -- (5) Không cho phép 2 folder cùng tên trong cùng 1 thư mục cha
    -- NULLS NOT DISTINCT: 2 folder gốc (parent_id = NULL) vẫn phải khác tên
                         CONSTRAINT uq_folder_name_in_parent
                             UNIQUE NULLS NOT DISTINCT (workspace_id, parent_id, name)
);

-- INDEX
CREATE INDEX idx_folders_workspace_id ON folders(workspace_id);
CREATE INDEX idx_folders_parent_id ON folders(parent_id);  -- (6)
CREATE INDEX idx_folders_workspace_parent
    ON folders(workspace_id, parent_id);  -- (7) composite index cho query phổ biến nhất