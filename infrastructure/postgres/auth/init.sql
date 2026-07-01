-- ============================================================
-- AUTH SERVICE DATABASE SCHEMA
-- Database: auth_db
-- ============================================================

-- Extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- ============================================================
-- TABLE: roles
-- ============================================================
CREATE TABLE IF NOT EXISTS roles (
                                     id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name        VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at  TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP NOT NULL DEFAULT NOW()
    );

-- ============================================================
-- TABLE: users
-- ============================================================
CREATE TABLE IF NOT EXISTS users (
                                     id           UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    username     VARCHAR(100) NOT NULL UNIQUE,
    email        VARCHAR(255) NOT NULL UNIQUE,
    password     VARCHAR(255) NOT NULL,
    full_name    VARCHAR(255),
    phone        VARCHAR(20),
    role_id      UUID NOT NULL REFERENCES roles(id),
    enabled      BOOLEAN NOT NULL DEFAULT TRUE,
    created_at   TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMP NOT NULL DEFAULT NOW()
    );

-- ============================================================
-- TABLE: refresh_tokens
-- ============================================================
CREATE TABLE IF NOT EXISTS refresh_tokens (
                                              id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    token       TEXT NOT NULL UNIQUE,
    user_id     UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    expiry_date TIMESTAMP NOT NULL,
    created_at  TIMESTAMP NOT NULL DEFAULT NOW()
    );

-- ============================================================
-- INDEXES
-- ============================================================
CREATE INDEX IF NOT EXISTS idx_users_email    ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);
CREATE INDEX IF NOT EXISTS idx_users_role_id  ON users(role_id);
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_user_id ON refresh_tokens(user_id);
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_token   ON refresh_tokens(token);

-- ============================================================
-- SEED DATA - Default Roles
-- ============================================================
INSERT INTO roles (id, name, description) VALUES
                                              ('11111111-1111-1111-1111-111111111111', 'ADMIN',            'Quản trị hệ thống'),
                                              ('22222222-2222-2222-2222-222222222222', 'DISPATCHER',       'Điều hành chuyến xe'),
                                              ('33333333-3333-3333-3333-333333333333', 'TICKET_AGENT',     'Nhân viên bán vé'),
                                              ('44444444-4444-4444-4444-444444444444', 'DRIVER',           'Tài xế'),
                                              ('55555555-5555-5555-5555-555555555555', 'CUSTOMER',         'Khách hàng'),
                                              ('66666666-6666-6666-6666-666666666666', 'AGENT',            'Đại lý bán vé')
    ON CONFLICT (name) DO NOTHING;

-- Default admin user (password: Admin@123)
INSERT INTO users (id, username, email, password, full_name, phone, role_id) VALUES
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
     'admin',
     'admin@busticket.vn',
     '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', -- Admin@123
     'System Admin',
     '0900000000',
     '11111111-1111-1111-1111-111111111111')
    ON CONFLICT (username) DO NOTHING;

-- ============================================================
-- TRIGGER: auto-update updated_at
-- ============================================================
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
RETURN NEW;
END;
$$ language 'plpgsql';

CREATE OR REPLACE TRIGGER update_users_updated_at
    BEFORE UPDATE ON users
                         FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE OR REPLACE TRIGGER update_roles_updated_at
    BEFORE UPDATE ON roles
                      FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
