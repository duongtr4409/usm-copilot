-- Migration: V12__add_user_account_role_column.sql
-- Adds a simple `role` column to user_accounts to support legacy JPA mapping used in tests
ALTER TABLE IF EXISTS user_accounts ADD COLUMN IF NOT EXISTS role VARCHAR(100);
