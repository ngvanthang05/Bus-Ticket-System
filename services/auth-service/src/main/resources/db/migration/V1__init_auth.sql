CREATE TABLE users (
    id              BIGSERIAL PRIMARY KEY,
    full_name       VARCHAR(150)        NOT NULL,
    email           VARCHAR(150)        NOT NULL UNIQUE,
    phone           VARCHAR(20)         UNIQUE,
    password        VARCHAR(255)        NOT NULL,
    role            VARCHAR(30)         NOT NULL DEFAULT 'KHACH_HANG',
    is_active       BOOLEAN             NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP           NOT NULL DEFAULT now(),
    updated_at      TIMESTAMP           NOT NULL DEFAULT now()
);

CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_role ON users(role);