-- ============================================================
-- Migration: V7__news_posts.sql
-- Task ID:   TASK-002
-- Author:    @DB-Admin
-- Date:      2026-04-21
-- Description: Create `news_posts` table for academy news and training posts.
-- ============================================================

CREATE TABLE IF NOT EXISTS news_posts (
    id                 UUID        NOT NULL DEFAULT gen_random_uuid(),
    title              VARCHAR(400) NOT NULL,
    slug               VARCHAR(300) NOT NULL,
    summary            TEXT,
    content            JSONB,
    author_id          UUID,
    scope_org_unit_id  UUID,
    is_published       BOOLEAN     NOT NULL DEFAULT FALSE,
    published_at       TIMESTAMP WITH TIME ZONE,
    status             VARCHAR(50) NOT NULL DEFAULT 'DRAFT',
    created_at         TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at         TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    created_by         VARCHAR(255),
    updated_by         VARCHAR(255),
    CONSTRAINT pk_news_posts PRIMARY KEY (id),
    CONSTRAINT uq_news_posts_slug UNIQUE (slug),
    CONSTRAINT fk_news_author FOREIGN KEY (author_id) REFERENCES user_accounts (id) ON DELETE SET NULL,
    CONSTRAINT fk_news_scope_org FOREIGN KEY (scope_org_unit_id) REFERENCES organization_unit (id) ON DELETE SET NULL,
    CONSTRAINT ck_news_status CHECK (status IN ('DRAFT','PUBLISHED','ARCHIVED'))
);

CREATE INDEX IF NOT EXISTS idx_news_posts_scope_org ON news_posts(scope_org_unit_id);
CREATE INDEX IF NOT EXISTS idx_news_posts_published_at ON news_posts(published_at) WHERE is_published = TRUE;
CREATE INDEX IF NOT EXISTS idx_news_posts_is_published ON news_posts(is_published);

-- ▶ auto-update updated_at trigger
DROP TRIGGER IF EXISTS trg_news_posts_updated_at ON news_posts;
CREATE TRIGGER trg_news_posts_updated_at
    BEFORE UPDATE ON news_posts
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
