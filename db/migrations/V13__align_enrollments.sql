-- ============================================================
-- Migration: V13__align_enrollments.sql
-- Author:    @DB-Admin
-- Date:      2026-04-22
-- Description: Ensure `enrollments.class_unit_id` exists, is UUID, has FK -> organization_unit(id)
-- and the unique constraint/index `uq_enrollments_student_class` exists. Idempotent.
-- ============================================================

DO $$
BEGIN
    -- 1) Add column `class_unit_id` if it does not exist
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = 'public' AND table_name = 'enrollments' AND column_name = 'class_unit_id'
    ) THEN
        ALTER TABLE public.enrollments ADD COLUMN class_unit_id uuid;
    END IF;

    -- 2) Ensure column type is uuid (safe cast if needed)
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = 'public' AND table_name = 'enrollments' AND column_name = 'class_unit_id' AND udt_name <> 'uuid'
    ) THEN
        ALTER TABLE public.enrollments ALTER COLUMN class_unit_id TYPE uuid USING class_unit_id::uuid;
    END IF;

    -- 3) Ensure there is a foreign key from enrollments(class_unit_id) -> organization_unit(id) ON DELETE CASCADE
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint c
        JOIN pg_class rel ON rel.oid = c.conrelid
        WHERE rel.relname = 'enrollments'
          AND c.contype = 'f'
          AND c.confrelid = (SELECT oid FROM pg_class WHERE relname = 'organization_unit')
          AND EXISTS (
              SELECT 1
              FROM unnest(c.conkey) AS ck(attnum)
              JOIN pg_attribute a ON a.attrelid = rel.oid AND a.attnum = ck.attnum
              WHERE a.attname = 'class_unit_id'
          )
    ) THEN
        ALTER TABLE public.enrollments
            ADD CONSTRAINT fk_enrollments_class_unit FOREIGN KEY (class_unit_id) REFERENCES public.organization_unit(id) ON DELETE CASCADE;
    END IF;

    -- 4) Ensure unique constraint on (student_profile_id, class_unit_id)
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'public' AND table_name = 'enrollments' AND column_name IN ('student_profile_id', 'class_unit_id')
        GROUP BY table_name HAVING COUNT(*) = 2
    ) THEN
        IF NOT EXISTS (
            SELECT 1 FROM pg_constraint c
            JOIN pg_class t ON t.oid = c.conrelid
            WHERE t.relname = 'enrollments' AND c.contype = 'u' AND c.conname = 'uq_enrollments_student_class'
        ) THEN
            ALTER TABLE public.enrollments ADD CONSTRAINT uq_enrollments_student_class UNIQUE (student_profile_id, class_unit_id);
        END IF;
    END IF;
END
$$ LANGUAGE plpgsql;

-- 5) Ensure useful indexes exist (idempotent)
CREATE INDEX IF NOT EXISTS idx_enrollments_class_unit_id ON public.enrollments (class_unit_id);
CREATE INDEX IF NOT EXISTS idx_enrollments_student_profile_id ON public.enrollments (student_profile_id);

-- Keep a conservative approach: do not drop or rename existing objects.
