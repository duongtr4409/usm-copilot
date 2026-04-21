-- ============================================================
-- Migration: V8__outbox.sql
-- Task ID:   TASK-002
-- Author:    @DB-Admin
-- Date:      2026-04-21
-- Description: Outbox table for reliable event publishing (polling-friendly indexes).
-- ============================================================

CREATE TABLE IF NOT EXISTS outbox (
    id             UUID        NOT NULL DEFAULT gen_random_uuid(),
    aggregate_type VARCHAR(150) NOT NULL,
    aggregate_id   UUID,
    event_type     VARCHAR(150) NOT NULL,
    payload        JSONB       NOT NULL,
    published      BOOLEAN     NOT NULL DEFAULT FALSE,
    tries          INTEGER     NOT NULL DEFAULT 0,
    created_at     TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    published_at   TIMESTAMP WITH TIME ZONE,
    CONSTRAINT pk_outbox PRIMARY KEY (id)
);

-- Index to efficiently poll for unpublished events in chronological order
CREATE INDEX IF NOT EXISTS idx_outbox_unpublished_created_at ON outbox (created_at) WHERE published = FALSE;
CREATE INDEX IF NOT EXISTS idx_outbox_published_created_at ON outbox (published, created_at);

COMMENT ON TABLE outbox IS 'Outbox table for domain events; poll unpublished rows ordered by created_at';
