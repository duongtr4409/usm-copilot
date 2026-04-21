-- ============================================================
-- Migration: V6__classes_enrollments.sql
-- Task ID:   TASK-002
-- Author:    @DB-Admin
-- Date:      2026-04-21
-- Description: Create `classes`, `enrollments`, and `class_assignments` tables. Enforce that classes map to OrganizationUnit.type = 'Lớp'.
-- ============================================================

-- ▶ Classes metadata (maps 1:1 to an OrganizationUnit of type 'Lớp')
CREATE TABLE IF NOT EXISTS classes (
    id               UUID        NOT NULL DEFAULT gen_random_uuid(),
    org_unit_id      UUID        NOT NULL,
    code             VARCHAR(100),
    title            VARCHAR(255),
    capacity         INTEGER,
    term             VARCHAR(100),
    teacher_staff_id UUID,
    status           VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    created_at       TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    created_by       VARCHAR(255),
    updated_by       VARCHAR(255),
    CONSTRAINT pk_classes PRIMARY KEY (id),
    CONSTRAINT uq_classes_org_unit UNIQUE (org_unit_id),
    CONSTRAINT fk_classes_org_unit FOREIGN KEY (org_unit_id) REFERENCES organization_unit (id) ON DELETE CASCADE,
    CONSTRAINT fk_classes_teacher FOREIGN KEY (teacher_staff_id) REFERENCES staff (id) ON DELETE SET NULL,
    CONSTRAINT ck_classes_capacity CHECK (capacity IS NULL OR capacity >= 0),
    CONSTRAINT ck_classes_status CHECK (status IN ('ACTIVE','INACTIVE','DELETED'))
);

CREATE INDEX IF NOT EXISTS idx_classes_org_unit_id ON classes(org_unit_id);
CREATE INDEX IF NOT EXISTS idx_classes_teacher_staff_id ON classes(teacher_staff_id);
CREATE INDEX IF NOT EXISTS idx_classes_status ON classes(status) WHERE status != 'DELETED';

-- ▶ Ensure referenced organization_unit is of type 'Lớp'
CREATE OR REPLACE FUNCTION class_org_unit_must_be_lop()
RETURNS TRIGGER AS $$
DECLARE
    utype TEXT;
BEGIN
    SELECT type INTO utype FROM organization_unit WHERE id = NEW.org_unit_id;
    IF utype IS NULL THEN
        RAISE EXCEPTION 'organization_unit % not found', NEW.org_unit_id;
    END IF;
    IF utype <> 'Lớp' THEN
        RAISE EXCEPTION 'classes.org_unit_id must reference an OrganizationUnit of type ''Lớp''';
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION class_org_unit_must_be_lop() IS 'Validates classes.org_unit_id references an OrganizationUnit of type Lớp';

DROP TRIGGER IF EXISTS trg_classes_org_unit_check ON classes;
CREATE TRIGGER trg_classes_org_unit_check
    BEFORE INSERT OR UPDATE ON classes
    FOR EACH ROW EXECUTE FUNCTION class_org_unit_must_be_lop();

-- ▶ auto-update updated_at trigger
DROP TRIGGER IF EXISTS trg_classes_updated_at ON classes;
CREATE TRIGGER trg_classes_updated_at
    BEFORE UPDATE ON classes
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- ▶ Enrollments: student_profile <-> classes
CREATE TABLE IF NOT EXISTS enrollments (
    id                 UUID        NOT NULL DEFAULT gen_random_uuid(),
    student_profile_id UUID        NOT NULL,
    class_id           UUID        NOT NULL,
    enrolled_at        TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    status             VARCHAR(50) NOT NULL DEFAULT 'ENROLLED',
    enrolled_by        UUID,
    created_at         TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at         TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT pk_enrollments PRIMARY KEY (id),
    CONSTRAINT uq_enrollments_student_class UNIQUE (student_profile_id, class_id),
    CONSTRAINT fk_enrollments_student FOREIGN KEY (student_profile_id) REFERENCES student_profile (id) ON DELETE CASCADE,
    CONSTRAINT fk_enrollments_class FOREIGN KEY (class_id) REFERENCES classes (id) ON DELETE CASCADE,
    CONSTRAINT ck_enrollments_status CHECK (status IN ('ENROLLED','PENDING','DROPPED'))
);

CREATE INDEX IF NOT EXISTS idx_enrollments_class_id ON enrollments(class_id);
CREATE INDEX IF NOT EXISTS idx_enrollments_student_profile_id ON enrollments(student_profile_id);
CREATE INDEX IF NOT EXISTS idx_enrollments_status ON enrollments(status);

-- ▶ auto-update updated_at trigger
DROP TRIGGER IF EXISTS trg_enrollments_updated_at ON enrollments;
CREATE TRIGGER trg_enrollments_updated_at
    BEFORE UPDATE ON enrollments
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- ▶ Class assignments (Staff ↔ Class many-to-many)
CREATE TABLE IF NOT EXISTS class_assignments (
    id          UUID        NOT NULL DEFAULT gen_random_uuid(),
    staff_id    UUID        NOT NULL,
    class_id    UUID        NOT NULL,
    role        VARCHAR(50) DEFAULT 'TEACHER',
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT pk_class_assignments PRIMARY KEY (id),
    CONSTRAINT uq_class_assignments_staff_class UNIQUE (staff_id, class_id),
    CONSTRAINT fk_class_assignments_staff FOREIGN KEY (staff_id) REFERENCES staff (id) ON DELETE CASCADE,
    CONSTRAINT fk_class_assignments_class FOREIGN KEY (class_id) REFERENCES classes (id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_class_assignments_staff_id ON class_assignments(staff_id);
CREATE INDEX IF NOT EXISTS idx_class_assignments_class_id ON class_assignments(class_id);

-- ▶ auto-update updated_at trigger
DROP TRIGGER IF EXISTS trg_class_assignments_updated_at ON class_assignments;
CREATE TRIGGER trg_class_assignments_updated_at
    BEFORE UPDATE ON class_assignments
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
