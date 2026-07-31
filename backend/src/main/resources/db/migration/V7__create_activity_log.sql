CREATE TABLE activity_logs (
                               id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                               actor_id        UUID,                    -- (1) user thực hiện hành động
                               actor_email     VARCHAR(255) NOT NULL,   -- (2) lưu email tại thời điểm đó
                               actor_name      VARCHAR(255) NOT NULL,   -- (3) lưu tên tại thời điểm đó
                               action          VARCHAR(100) NOT NULL,   -- (4) loại hành động
                               workspace_id    UUID,                    -- (5) workspace liên quan
                               workspace_name  VARCHAR(255),            -- (6) lưu tên workspace tại thời điểm đó
                               target_type     VARCHAR(50),             -- (7) loại object: FILE/FOLDER/MEMBER/WORKSPACE
                               target_id       UUID,                    -- (8) id của object
                               target_name     VARCHAR(255),            -- (9) tên object tại thời điểm đó
                               metadata        JSONB,                   -- (10) data bổ sung dạng JSON
                               ip_address      VARCHAR(50),             -- (11) IP của request
                               user_agent      VARCHAR(500),            -- (12) browser/client info
                               created_at      TIMESTAMP NOT NULL DEFAULT NOW(),

    -- (13) Không có updated_at — activity log là immutable
    -- Không extends BaseEntity vì không cần updated_at/created_by/updated_by

                               CONSTRAINT fk_activity_actor
                                   FOREIGN KEY (actor_id) REFERENCES users(id)
                                       ON DELETE SET NULL  -- (14) xóa user → giữ log, actor_id = null
);

-- INDEX cho các query phổ biến
CREATE INDEX idx_activity_logs_workspace_id
    ON activity_logs(workspace_id);

CREATE INDEX idx_activity_logs_actor_id
    ON activity_logs(actor_id);

-- (15) Composite index cho query "log trong workspace, sắp xếp theo thời gian"
CREATE INDEX idx_activity_logs_workspace_time
    ON activity_logs(workspace_id, created_at DESC);

-- (16) Index cho tìm kiếm theo action type
CREATE INDEX idx_activity_logs_action
    ON activity_logs(action);