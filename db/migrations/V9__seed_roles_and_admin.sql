-- ============================================================
-- Migration: V9__seed_roles_and_admin.sql
-- Task ID:   TASK-002
-- Author:    @DB-Admin
-- Date:      2026-04-21
-- Description: Seed essential roles and a seeded admin user (password placeholder).
-- Note: Replace placeholder password_hash and rotate credentials after deployment.
-- ============================================================

-- ▶ Seed roles (idempotent)
INSERT INTO roles (id, name, description, created_at, updated_at)
VALUES (gen_random_uuid(), 'ADMIN', 'System administrator - full privileges', NOW(), NOW())
ON CONFLICT (name) DO NOTHING;

INSERT INTO roles (id, name, description, created_at, updated_at)
VALUES (gen_random_uuid(), 'STUDENT', 'Student role - student portal access', NOW(), NOW())
ON CONFLICT (name) DO NOTHING;

INSERT INTO roles (id, name, description, created_at, updated_at)
VALUES (gen_random_uuid(), 'STAFF', 'Staff role - staff-level access', NOW(), NOW())
ON CONFLICT (name) DO NOTHING;

INSERT INTO roles (id, name, description, created_at, updated_at)
VALUES (gen_random_uuid(), 'CLASS_ADMIN', 'Class administrator - limited class operations', NOW(), NOW())
ON CONFLICT (name) DO NOTHING;

-- ▶ Seed admin account (idempotent)
-- NOTE: Replace '<REPLACE_WITH_SECURE_HASH>' with a production bcrypt/argon2 hash before use.
INSERT INTO user_accounts (id, username, email, password_hash, status, created_at, updated_at)
VALUES (gen_random_uuid(), 'admin', 'admin@example.com', '<REPLACE_WITH_SECURE_HASH>', 'ACTIVE', NOW(), NOW())
ON CONFLICT (username) DO NOTHING;

-- ▶ Grant ADMIN role to admin account (idempotent)
WITH admin_user AS (
    SELECT id AS user_id FROM user_accounts WHERE username = 'admin'
), admin_role AS (
    SELECT id AS role_id FROM roles WHERE name = 'ADMIN'
)
INSERT INTO user_roles (user_account_id, role_id, created_at)
SELECT admin_user.user_id, admin_role.role_id, NOW()
FROM admin_user, admin_role
ON CONFLICT (user_account_id, role_id) DO NOTHING;

-- ▶ Guidance comment (kept in SQL file)
-- IMPORTANT:
-- - The seeded admin password is a placeholder. Rotate immediately:
--   1) Update the `user_accounts.password_hash` with a secure bcrypt/argon2 hash.
--   2) Alternatively, delete the seeded admin and create a new admin user via the application with a strong password.
-- - Consider removing or locking this seeded account in production environments.
