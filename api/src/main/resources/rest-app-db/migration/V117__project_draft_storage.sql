ALTER TABLE projects
    ADD COLUMN IF NOT EXISTS draft_data TEXT;

ALTER TABLE projects
    ADD COLUMN IF NOT EXISTS draft_version INTEGER;

UPDATE projects
SET draft_version = 1
WHERE draft_version IS NULL;
