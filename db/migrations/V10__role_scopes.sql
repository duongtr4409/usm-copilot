-- ============================================================
-- Migration: V10__role_scopes.sql
-- Task ID:   TASK-013
-- Author:    @DB-Admin / @Tech-lead
-- Date:      2026-04-21
-- Description: Add scoped role assignment table to support role assignments tied to resources (ORG, CLASS, etc.).
-- ============================================================

-- ▶ Create user_role_scopes table: optional scoped role assignments
CREATE TABLE IF NOT EXISTS user_role_scopes (
    id               UUID NOT NULL DEFAULT gen_random_uuid(),
    user_account_id  UUID NOT NULL,
    role_id          UUID NOT NULL,
    scope_type       VARCHAR(50) NOT NULL, -- e.g. 'ORG', 'CLASS'
    scope_id         UUID NULL,            -- references the scoped object (org_id, class_id)
    created_at       TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    created_by       VARCHAR(255),
    CONSTRAINT pk_user_role_scopes PRIMARY KEY (id),
    CONSTRAINT fk_urs_user FOREIGN KEY (user_account_id) REFERENCES user_accounts (id) ON DELETE CASCADE,
    CONSTRAINT fk_urs_role FOREIGN KEY (role_id) REFERENCES roles (id) ON DELETE CASCADE
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_user_role_scopes_user_role_scope ON user_role_scopes(user_account_id, role_id, scope_type, scope_id);
CREATE INDEX IF NOT EXISTS idx_user_role_scopes_user_id ON user_role_scopes(user_account_id);
CREATE INDEX IF NOT EXISTS idx_user_role_scopes_role_id ON user_role_scopes(role_id);
CREATE INDEX IF NOT EXISTS idx_user_role_scopes_scope ON user_role_scopes(scope_type, scope_id);

-- ▶ Guidance / example (do not run automatically in prod)
-- Example: grant CLASS_ADMIN for a specific class
-- INSERT INTO user_role_scopes (user_account_id, role_id, scope_type, scope_id)
-- SELECT u.id, r.id, 'CLASS', '00000000-0000-0000-0000-000000000000'::UUID
-- FROM user_accounts u, roles r WHERE u.username = 'alice' AND r.name = 'CLASS_ADMIN'
-- ON CONFLICT (user_account_id, role_id, scope_type, scope_id) DO NOTHING;

-- Note: This migration keeps existing `user_roles` for global roles. Use `user_role_scopes` for scoped assignments.
