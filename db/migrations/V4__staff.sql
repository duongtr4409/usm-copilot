-- ============================================================
-- Migration: V4__staff.sql
-- Task ID:   TASK-002
-- Author:    @DB-Admin
-- Date:      2026-04-21
-- Description: Create `staff` table and enforce that staff.unit_id cannot reference OrganizationUnit.type = 'Lớp'.
-- ============================================================

CREATE TABLE IF NOT EXISTS staff (
    id            UUID        NOT NULL DEFAULT gen_random_uuid(),
    account_id    UUID,
    staff_number  VARCHAR(100),
    full_name     VARCHAR(255) NOT NULL,
    position      VARCHAR(255),
    unit_id       UUID,
    contact       JSONB,
    status        VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    created_at    TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    created_by    VARCHAR(255),
    updated_by    VARCHAR(255),
    CONSTRAINT pk_staff PRIMARY KEY (id),
    CONSTRAINT uq_staff_staff_number UNIQUE (staff_number),
    CONSTRAINT ck_staff_status CHECK (status IN ('ACTIVE', 'INACTIVE', 'DELETED')),
    CONSTRAINT fk_staff_account FOREIGN KEY (account_id) REFERENCES user_accounts (id) ON DELETE SET NULL,
    CONSTRAINT fk_staff_unit FOREIGN KEY (unit_id) REFERENCES organization_unit (id) ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_staff_unit_id ON staff(unit_id);
CREATE INDEX IF NOT EXISTS idx_staff_account_id ON staff(account_id);
CREATE INDEX IF NOT EXISTS idx_staff_status ON staff(status) WHERE status != 'DELETED';

-- ▶ Trigger: prevent assigning staff.unit_id to an OrganizationUnit of type 'Lớp'
CREATE OR REPLACE FUNCTION staff_unit_not_class()
RETURNS TRIGGER AS $$
DECLARE
    unit_type TEXT;
BEGIN
    IF NEW.unit_id IS NULL THEN
        RETURN NEW;
    END IF;

    SELECT type INTO unit_type FROM organization_unit WHERE id = NEW.unit_id;
    IF unit_type IS NULL THEN
        RAISE EXCEPTION 'organization_unit % not found', NEW.unit_id;
    END IF;

    IF unit_type = 'Lớp' THEN
        RAISE EXCEPTION 'staff.unit_id cannot reference an OrganizationUnit of type ''Lớp''';
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION staff_unit_not_class() IS 'Prevents staff.unit_id pointing to a Lớp (class) organization unit';

DROP TRIGGER IF EXISTS trg_staff_unit_check ON staff;
CREATE TRIGGER trg_staff_unit_check
    BEFORE INSERT OR UPDATE ON staff
    FOR EACH ROW EXECUTE FUNCTION staff_unit_not_class();

-- ▶ auto-update updated_at trigger
DROP TRIGGER IF EXISTS trg_staff_updated_at ON staff;
CREATE TRIGGER trg_staff_updated_at
    BEFORE UPDATE ON staff
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
