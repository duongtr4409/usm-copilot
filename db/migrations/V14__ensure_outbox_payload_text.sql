-- ============================================================
-- Migration: V14__ensure_outbox_payload_text.sql
-- Author:    @DB-Admin
-- Date:      2026-04-22
-- Description: Ensure `outbox.payload` column is TEXT. If it's JSON/JSONB, convert safely using an ALTER TYPE USING cast.
-- This migration is idempotent and avoids destructive drops/renames.
-- ============================================================

DO $$
DECLARE
    _udt TEXT;
    _is_nullable TEXT;
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'public' AND table_name = 'outbox' AND column_name = 'payload'
    ) THEN
        -- Add as TEXT if missing
        ALTER TABLE public.outbox ADD COLUMN payload TEXT;
    ELSE
        SELECT udt_name, is_nullable INTO _udt, _is_nullable
        FROM information_schema.columns
        WHERE table_schema = 'public' AND table_name = 'outbox' AND column_name = 'payload';

        -- If the column is jsonb or json, convert to text using a safe cast
        IF _udt = 'jsonb' OR _udt = 'json' THEN
            -- Use ALTER TYPE ... USING which is transactional and preserves data
            ALTER TABLE public.outbox ALTER COLUMN payload TYPE text USING payload::text;
        END IF;

        -- If column is not declared NOT NULL but there are no NULLs, set NOT NULL to match expectations
        IF _is_nullable = 'YES' THEN
            IF NOT EXISTS (SELECT 1 FROM public.outbox WHERE payload IS NULL) THEN
                ALTER TABLE public.outbox ALTER COLUMN payload SET NOT NULL;
            END IF;
        END IF;
    END IF;
END
$$ LANGUAGE plpgsql;

-- Keep indexes and other objects unchanged. This migration avoids dropping the existing column to prevent accidental data loss.
