-- =============================================
-- BẢNG files — lưu metadata của file
-- =============================================
CREATE TABLE files (
                       id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                       original_name   VARCHAR(255) NOT NULL,   -- (1) tên gốc user upload "báo cáo Q1.pdf"
                       storage_key     VARCHAR(500) NOT NULL UNIQUE, -- (2) đường dẫn trong MinIO
                       content_type    VARCHAR(100) NOT NULL,   -- (3) MIME type "application/pdf"
                       size            BIGINT NOT NULL,         -- (4) kích thước bytes
                       extension       VARCHAR(20),             -- (5) "pdf", "docx", "png"...
                       workspace_id    UUID NOT NULL,
                       folder_id       UUID,                    -- (6) nullable: file ở root workspace
                       uploaded_by_id  UUID NOT NULL,
                       is_deleted      BOOLEAN NOT NULL DEFAULT FALSE,
                       deleted_at      TIMESTAMP,
                       created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
                       updated_at      TIMESTAMP NOT NULL DEFAULT NOW(),
                       created_by      VARCHAR(255),
                       updated_by      VARCHAR(255),

                       CONSTRAINT fk_file_workspace
                           FOREIGN KEY (workspace_id) REFERENCES workspaces(id)
                               ON DELETE CASCADE,

                       CONSTRAINT fk_file_folder
                           FOREIGN KEY (folder_id) REFERENCES folders(id)
                               ON DELETE SET NULL, -- (7) xóa folder → file vẫn còn, folder_id = null

                       CONSTRAINT fk_file_uploader
                           FOREIGN KEY (uploaded_by_id) REFERENCES users(id)
                               ON DELETE SET NULL
);

-- INDEX
CREATE INDEX idx_files_workspace_id ON files(workspace_id);
CREATE INDEX idx_files_folder_id ON files(folder_id);
CREATE INDEX idx_files_uploaded_by ON files(uploaded_by_id);
-- (8) Full-text search tên file
CREATE INDEX idx_files_original_name ON files
    USING gin(to_tsvector('simple', original_name));