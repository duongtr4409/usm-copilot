-- Seed data for RBAC unit/integration tests
-- Creates a test user with a scoped CLASS_ADMIN assignment for a known class id

INSERT INTO roles (id, name)
SELECT gen_random_uuid(), 'CLASS_ADMIN'
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE name = 'CLASS_ADMIN');

INSERT INTO user_accounts (id, username, password_hash, status)
SELECT gen_random_uuid(), 'test-class-admin', 'test-hash', 'ACTIVE'
WHERE NOT EXISTS (SELECT 1 FROM user_accounts WHERE username = 'test-class-admin');

-- grant CLASS_ADMIN scoped to a specific class id used by tests
INSERT INTO user_role_scopes (user_account_id, role_id, scope_type, scope_id, created_by)
SELECT ua.id, r.id, 'CLASS', '00000000-0000-0000-0000-000000000001'::UUID, 'migration'
FROM user_accounts ua, roles r
WHERE ua.username = 'test-class-admin' AND r.name = 'CLASS_ADMIN'
ON CONFLICT (user_account_id, role_id, scope_type, scope_id) DO NOTHING;
