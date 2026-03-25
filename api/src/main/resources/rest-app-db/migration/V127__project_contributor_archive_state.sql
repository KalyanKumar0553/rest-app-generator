ALTER TABLE project_contributors
    ADD COLUMN IF NOT EXISTS disabled BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE project_contributors
    ADD COLUMN IF NOT EXISTS disabled_at TIMESTAMPTZ;

CREATE INDEX IF NOT EXISTS idx_project_contributors_user_disabled
    ON project_contributors (user_id, disabled);
