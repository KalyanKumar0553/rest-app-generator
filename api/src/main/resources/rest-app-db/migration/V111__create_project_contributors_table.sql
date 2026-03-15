CREATE TABLE IF NOT EXISTS project_contributors (
    id UUID PRIMARY KEY,
    project_id UUID NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    user_id VARCHAR(100) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_project_contributors_project_user UNIQUE (project_id, user_id)
);

CREATE INDEX IF NOT EXISTS idx_project_contributors_user
    ON project_contributors (user_id);

CREATE INDEX IF NOT EXISTS idx_project_contributors_project
    ON project_contributors (project_id);
