-- ============================================================
-- Migration: V2__user_accounts_and_roles.sql
-- Task ID:   TASK-002
-- Author:    @DB-Admin
-- Date:      2026-04-21
-- Description: Create `roles`, `user_accounts`, and `user_roles` tables.
-- ============================================================

-- ▶ Roles table
CREATE TABLE IF NOT EXISTS roles (
    id          UUID        NOT NULL DEFAULT gen_random_uuid(),
    name        VARCHAR(100) NOT NULL,
    description TEXT,
    status      VARCHAR(50)  NOT NULL DEFAULT 'ACTIVE',
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    created_by  VARCHAR(255),
    updated_by  VARCHAR(255),
    CONSTRAINT pk_roles PRIMARY KEY (id),
    CONSTRAINT uq_roles_name UNIQUE (name),
    CONSTRAINT ck_roles_status CHECK (status IN ('ACTIVE', 'INACTIVE', 'DELETED'))
);

CREATE INDEX IF NOT EXISTS idx_roles_status ON roles(status) WHERE status != 'DELETED';
CREATE INDEX IF NOT EXISTS idx_roles_created_at ON roles(created_at DESC);

-- Trigger for roles.updated_at
DROP TRIGGER IF EXISTS trg_roles_updated_at ON roles;
CREATE TRIGGER trg_roles_updated_at
    BEFORE UPDATE ON roles
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- ▶ User accounts
CREATE TABLE IF NOT EXISTS user_accounts (
    id            UUID        NOT NULL DEFAULT gen_random_uuid(),
    username      VARCHAR(150) NOT NULL,
    email         VARCHAR(255),
    password_hash VARCHAR(255) NOT NULL,
    status        VARCHAR(50)  NOT NULL DEFAULT 'ACTIVE',
    last_login    TIMESTAMP WITH TIME ZONE,
    metadata      JSONB,
    created_at    TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    created_by    VARCHAR(255),
    updated_by    VARCHAR(255),
    CONSTRAINT pk_user_accounts PRIMARY KEY (id),
    CONSTRAINT uq_user_accounts_username UNIQUE (username),
    CONSTRAINT uq_user_accounts_email UNIQUE (email),
    CONSTRAINT ck_user_accounts_status CHECK (status IN ('ACTIVE', 'INACTIVE', 'DELETED'))
);

-- Case-insensitive uniqueness helper (optional enforcement)
CREATE UNIQUE INDEX IF NOT EXISTS idx_user_accounts_username_ci ON user_accounts (lower(username));

CREATE INDEX IF NOT EXISTS idx_user_accounts_status ON user_accounts(status) WHERE status != 'DELETED';
CREATE INDEX IF NOT EXISTS idx_user_accounts_created_at ON user_accounts(created_at DESC);

-- Trigger for user_accounts.updated_at
DROP TRIGGER IF EXISTS trg_user_accounts_updated_at ON user_accounts;
CREATE TRIGGER trg_user_accounts_updated_at
    BEFORE UPDATE ON user_accounts
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- ▶ User ↔ Role mapping
CREATE TABLE IF NOT EXISTS user_roles (
    user_account_id UUID NOT NULL,
    role_id         UUID NOT NULL,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT pk_user_roles PRIMARY KEY (user_account_id, role_id),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_account_id) REFERENCES user_accounts (id) ON DELETE CASCADE,
    CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES roles (id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_user_roles_role_id ON user_roles(role_id);
CREATE INDEX IF NOT EXISTS idx_user_roles_user_id ON user_roles(user_account_id);
