-- =============================================
-- BẢNG share_links — public link chia sẻ
-- =============================================
CREATE TABLE share_links (
                             id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                             file_id       UUID NOT NULL,
                             token         VARCHAR(100) NOT NULL UNIQUE, -- (1) token ngẫu nhiên trong URL
                             created_by_id UUID NOT NULL,
                             password      VARCHAR(255),                 -- (2) optional: bảo vệ bằng password
                             expires_at    TIMESTAMP,                    -- (3) optional: hết hạn sau N ngày
                             is_active     BOOLEAN NOT NULL DEFAULT TRUE,-- (4) có thể tắt link mà không xóa
                             download_count INT NOT NULL DEFAULT 0,      -- (5) đếm số lượt tải
                             max_downloads  INT,                         -- (6) optional: giới hạn lượt tải
                             created_at    TIMESTAMP NOT NULL DEFAULT NOW(),
                             updated_at    TIMESTAMP NOT NULL DEFAULT NOW(),
                             created_by    VARCHAR(255),
                             updated_by    VARCHAR(255),

                             CONSTRAINT fk_share_link_file
                                 FOREIGN KEY (file_id) REFERENCES files(id)
                                     ON DELETE CASCADE,   -- xóa file → xóa luôn link

                             CONSTRAINT fk_share_link_creator
                                 FOREIGN KEY (created_by_id) REFERENCES users(id)
                                     ON DELETE CASCADE
);

-- =============================================
-- BẢNG file_permissions — chia sẻ riêng với user
-- =============================================
CREATE TABLE file_permissions (
                                  id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                  file_id         UUID NOT NULL,
                                  user_id         UUID NOT NULL,              -- user được chia sẻ
                                  shared_by_id    UUID NOT NULL,              -- user chia sẻ
                                  permission      VARCHAR(50) NOT NULL,       -- VIEW/EDIT/DOWNLOAD/DELETE
                                  expires_at      TIMESTAMP,                  -- (7) optional: hết hạn
                                  created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
                                  updated_at      TIMESTAMP NOT NULL DEFAULT NOW(),
                                  created_by      VARCHAR(255),
                                  updated_by      VARCHAR(255),

    -- (8) 1 user chỉ có 1 permission record cho 1 file
                                  CONSTRAINT uq_file_permission UNIQUE (file_id, user_id),

                                  CONSTRAINT fk_permission_file
                                      FOREIGN KEY (file_id) REFERENCES files(id)
                                          ON DELETE CASCADE,

                                  CONSTRAINT fk_permission_user
                                      FOREIGN KEY (user_id) REFERENCES users(id)
                                          ON DELETE CASCADE,

                                  CONSTRAINT fk_permission_sharer
                                      FOREIGN KEY (shared_by_id) REFERENCES users(id)
                                          ON DELETE SET NULL
);

-- INDEX
CREATE INDEX idx_share_links_token ON share_links(token);
CREATE INDEX idx_share_links_file_id ON share_links(file_id);
CREATE INDEX idx_file_permissions_file_id ON file_permissions(file_id);
CREATE INDEX idx_file_permissions_user_id ON file_permissions(user_id);
-- (9) Index tìm tất cả file được chia sẻ với 1 user
CREATE INDEX idx_file_permissions_user_file
    ON file_permissions(user_id, file_id);