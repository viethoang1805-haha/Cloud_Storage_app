-- =============================================
-- BẢNG roles — lưu các role của hệ thống
-- =============================================
CREATE TABLE roles (
                       id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),  -- (1)
                       name        VARCHAR(50) NOT NULL UNIQUE,                 -- ROLE_USER, ROLE_ADMIN
                       created_at  TIMESTAMP NOT NULL DEFAULT NOW(),
                       updated_at  TIMESTAMP NOT NULL DEFAULT NOW(),
                       created_by  VARCHAR(255),
                       updated_by  VARCHAR(255)
);

-- =============================================
-- BẢNG users — lưu thông tin người dùng
-- =============================================
CREATE TABLE users (
                       id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                       email           VARCHAR(255) NOT NULL UNIQUE,  -- (2)
                       password        VARCHAR(255) NOT NULL,          -- đã bcrypt hash
                       full_name       VARCHAR(255) NOT NULL,
                       avatar_url      VARCHAR(500),                   -- link ảnh đại diện (nullable)
                       is_enabled      BOOLEAN NOT NULL DEFAULT TRUE,  -- (3) tài khoản có bị khóa không
                       storage_used    BIGINT NOT NULL DEFAULT 0,      -- (4) dung lượng đã dùng (bytes)
                       storage_limit   BIGINT NOT NULL DEFAULT 5368709120, -- (5) giới hạn 5GB mặc định
                       created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
                       updated_at      TIMESTAMP NOT NULL DEFAULT NOW(),
                       created_by      VARCHAR(255),
                       updated_by      VARCHAR(255)
);

-- =============================================
-- BẢNG user_roles — quan hệ nhiều-nhiều User ↔ Role
-- =============================================
CREATE TABLE user_roles (
                            user_id UUID NOT NULL,
                            role_id UUID NOT NULL,
                            PRIMARY KEY (user_id, role_id),                         -- (6)
                            CONSTRAINT fk_user_roles_user
                                FOREIGN KEY (user_id) REFERENCES users(id)
                                    ON DELETE CASCADE,                                  -- (7)
                            CONSTRAINT fk_user_roles_role
                                FOREIGN KEY (role_id) REFERENCES roles(id)
                                    ON DELETE CASCADE
);

-- =============================================
-- BẢNG refresh_tokens — lưu refresh token
-- =============================================
CREATE TABLE refresh_tokens (
                                id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                user_id     UUID NOT NULL,
                                token       VARCHAR(500) NOT NULL UNIQUE,
                                expires_at  TIMESTAMP NOT NULL,                         -- (8)
                                is_revoked  BOOLEAN NOT NULL DEFAULT FALSE,             -- (9)
                                created_at  TIMESTAMP NOT NULL DEFAULT NOW(),
                                updated_at  TIMESTAMP NOT NULL DEFAULT NOW(),
                                created_by  VARCHAR(255),
                                updated_by  VARCHAR(255),
                                CONSTRAINT fk_refresh_token_user
                                    FOREIGN KEY (user_id) REFERENCES users(id)
                                        ON DELETE CASCADE
);

-- =============================================
-- INDEX — tăng tốc query thường dùng
-- =============================================
CREATE INDEX idx_users_email ON users(email);               -- (10)
CREATE INDEX idx_refresh_tokens_token ON refresh_tokens(token);
CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens(user_id);

-- =============================================
-- DATA MẶC ĐỊNH — seed roles và admin
-- =============================================
INSERT INTO roles (id, name)
VALUES
    (gen_random_uuid(), 'ROLE_USER'),
    (gen_random_uuid(), 'ROLE_ADMIN');                      -- (11)