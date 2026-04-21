-- ============================================================
-- Migration: V5__student_profiles.sql
-- Task ID:   TASK-002
-- Author:    @DB-Admin
-- Date:      2026-04-21
-- Description: Create `student_profile` table and 1:1 link to user_accounts.
-- ============================================================

CREATE TABLE IF NOT EXISTS student_profile (
    id              UUID        NOT NULL DEFAULT gen_random_uuid(),
    account_id      UUID        NOT NULL,
    student_number  VARCHAR(100),
    first_name      VARCHAR(200),
    last_name       VARCHAR(200),
    dob             DATE,
    contact         JSONB,
    metadata        JSONB,
    status          VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    created_by      VARCHAR(255),
    updated_by      VARCHAR(255),
    CONSTRAINT pk_student_profile PRIMARY KEY (id),
    CONSTRAINT uq_student_profile_account UNIQUE (account_id),
    CONSTRAINT uq_student_profile_student_number UNIQUE (student_number),
    CONSTRAINT fk_student_profile_account FOREIGN KEY (account_id) REFERENCES user_accounts (id) ON DELETE CASCADE,
    CONSTRAINT ck_student_profile_status CHECK (status IN ('ACTIVE', 'INACTIVE', 'DELETED'))
);

CREATE INDEX IF NOT EXISTS idx_student_profile_student_number ON student_profile(student_number);
CREATE INDEX IF NOT EXISTS idx_student_profile_status ON student_profile(status) WHERE status != 'DELETED';

-- ▶ auto-update updated_at trigger
DROP TRIGGER IF EXISTS trg_student_profile_updated_at ON student_profile;
CREATE TRIGGER trg_student_profile_updated_at
    BEFORE UPDATE ON student_profile
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
