-- ============================================================
-- Migration: V3__organization_units.sql
-- Task ID:   TASK-002
-- Author:    @DB-Admin
-- Date:      2026-04-21
-- Description: Create hierarchical `organization_unit` with `ltree`-based materialized path.
-- ============================================================

CREATE TABLE IF NOT EXISTS organization_unit (
    id          UUID        NOT NULL DEFAULT gen_random_uuid(),
    name        VARCHAR(255) NOT NULL,
    code        VARCHAR(100),
    type        VARCHAR(50)  NOT NULL,
    parent_id   UUID,
    path        ltree,
    metadata    JSONB,
    status      VARCHAR(50)  NOT NULL DEFAULT 'ACTIVE',
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    created_by  VARCHAR(255),
    updated_by  VARCHAR(255),
    CONSTRAINT pk_organization_unit PRIMARY KEY (id),
    CONSTRAINT uq_organization_unit_code_parent UNIQUE (parent_id, code),
    CONSTRAINT ck_organization_unit_type CHECK (type IN ('Phòng','Ban','Văn Phòng','Khoa','Trung Tâm','Lớp')),
    CONSTRAINT ck_organization_unit_status CHECK (status IN ('ACTIVE', 'INACTIVE', 'DELETED')),
    CONSTRAINT fk_organization_unit_parent FOREIGN KEY (parent_id) REFERENCES organization_unit (id) ON DELETE RESTRICT
);

-- Indexes for tree and lookups
CREATE INDEX IF NOT EXISTS idx_organization_unit_parent_id ON organization_unit(parent_id);
CREATE INDEX IF NOT EXISTS idx_organization_unit_name ON organization_unit(name);
CREATE INDEX IF NOT EXISTS idx_organization_unit_path_gist ON organization_unit USING GIST (path);

CREATE INDEX IF NOT EXISTS idx_organization_unit_status ON organization_unit(status) WHERE status != 'DELETED';
CREATE INDEX IF NOT EXISTS idx_organization_unit_created_at ON organization_unit(created_at DESC);

-- ▶ Function to maintain `path` (materialized path using id labels)
CREATE OR REPLACE FUNCTION org_unit_set_path()
RETURNS TRIGGER AS $$
DECLARE
    parent_path_text TEXT;
BEGIN
    -- NEW.id has default applied before BEFORE triggers, so we can use it
    IF NEW.parent_id IS NOT NULL THEN
        SELECT path::text INTO parent_path_text FROM organization_unit WHERE id = NEW.parent_id;
        IF parent_path_text IS NULL THEN
            RAISE EXCEPTION 'Parent organization_unit % not found or has null path', NEW.parent_id;
        END IF;
        NEW.path := (parent_path_text || '.' || replace(NEW.id::text, '-', '_'))::ltree;
    ELSE
        NEW.path := (replace(NEW.id::text, '-', '_'))::ltree;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION org_unit_set_path() IS 'BEFORE INSERT/UPDATE trigger to set/maintain ltree path for organization_unit';

-- ▶ Trigger to set path
DROP TRIGGER IF EXISTS trg_organization_unit_set_path ON organization_unit;
CREATE TRIGGER trg_organization_unit_set_path
    BEFORE INSERT OR UPDATE ON organization_unit
    FOR EACH ROW EXECUTE FUNCTION org_unit_set_path();

-- ▶ auto-update updated_at trigger
DROP TRIGGER IF EXISTS trg_organization_unit_updated_at ON organization_unit;
CREATE TRIGGER trg_organization_unit_updated_at
    BEFORE UPDATE ON organization_unit
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
