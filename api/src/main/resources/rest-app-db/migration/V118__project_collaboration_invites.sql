ALTER TABLE projects
    ADD COLUMN IF NOT EXISTS invite_token VARCHAR(64);

ALTER TABLE project_contributors
    ADD COLUMN IF NOT EXISTS can_edit_draft BOOLEAN DEFAULT FALSE;

ALTER TABLE project_contributors
    ADD COLUMN IF NOT EXISTS can_generate BOOLEAN DEFAULT FALSE;

ALTER TABLE project_contributors
    ADD COLUMN IF NOT EXISTS can_manage_collaboration BOOLEAN DEFAULT FALSE;

UPDATE project_contributors
SET can_edit_draft = TRUE
WHERE can_edit_draft IS NULL;

UPDATE project_contributors
SET can_generate = TRUE
WHERE can_generate IS NULL;

UPDATE project_contributors
SET can_manage_collaboration = FALSE
WHERE can_manage_collaboration IS NULL;

CREATE TABLE IF NOT EXISTS project_collaboration_requests (
    id UUID PRIMARY KEY,
    project_id UUID NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    requester_id VARCHAR(100) NOT NULL,
    status VARCHAR(32) NOT NULL,
    requested_can_edit_draft BOOLEAN NOT NULL DEFAULT FALSE,
    requested_can_generate BOOLEAN NOT NULL DEFAULT FALSE,
    requested_can_manage_collaboration BOOLEAN NOT NULL DEFAULT FALSE,
    granted_can_edit_draft BOOLEAN NOT NULL DEFAULT FALSE,
    granted_can_generate BOOLEAN NOT NULL DEFAULT FALSE,
    granted_can_manage_collaboration BOOLEAN NOT NULL DEFAULT FALSE,
    reviewed_by VARCHAR(100),
    reviewed_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_project_collaboration_requests_project_id
    ON project_collaboration_requests(project_id);

CREATE INDEX IF NOT EXISTS idx_project_collaboration_requests_requester_id
    ON project_collaboration_requests(requester_id);
