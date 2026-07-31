CREATE TABLE notifications (
                               id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                               user_id     UUID NOT NULL,          -- (1) người nhận thông báo
                               title       VARCHAR(255) NOT NULL,
                               message     TEXT NOT NULL,
                               type        VARCHAR(50) NOT NULL,   -- (2) loại thông báo
                               is_read     BOOLEAN NOT NULL DEFAULT FALSE,
                               read_at     TIMESTAMP,
    -- (3) reference đến object liên quan
                               ref_type    VARCHAR(50),            -- "FILE", "WORKSPACE", "MEMBER"
                               ref_id      UUID,                   -- id của object
                               created_at  TIMESTAMP NOT NULL DEFAULT NOW(),
                               updated_at  TIMESTAMP NOT NULL DEFAULT NOW(),
                               created_by  VARCHAR(255),
                               updated_by  VARCHAR(255),

                               CONSTRAINT fk_notification_user
                                   FOREIGN KEY (user_id) REFERENCES users(id)
                                       ON DELETE CASCADE
);

CREATE INDEX idx_notifications_user_id
    ON notifications(user_id);

CREATE INDEX idx_notifications_user_unread
    ON notifications(user_id, is_read)
    WHERE is_read = false;  -- (4) Partial index — chỉ index record chưa đọc