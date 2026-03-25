CREATE TABLE IF NOT EXISTS project_draft_versions (
    id UUID PRIMARY KEY,
    project_id UUID NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    draft_version INTEGER NOT NULL,
    draft_data TEXT NOT NULL,
    yaml TEXT NOT NULL,
    generator VARCHAR(50),
    created_by_user_id VARCHAR(100) NOT NULL,
    restored_from_version_id UUID,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uq_project_draft_versions_project_version UNIQUE (project_id, draft_version)
);

CREATE INDEX IF NOT EXISTS idx_project_draft_versions_project_created
    ON project_draft_versions (project_id, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_project_draft_versions_project_version
    ON project_draft_versions (project_id, draft_version DESC);

INSERT INTO project_draft_versions (
    id,
    project_id,
    draft_version,
    draft_data,
    yaml,
    generator,
    created_by_user_id,
    restored_from_version_id,
    created_at
)
SELECT
    (
        substr(md5(p.id::text || ':' || COALESCE(p.draft_version, 1)::text || ':' || COALESCE(p.updated_at::text, p.created_at::text, NOW()::text)), 1, 8) || '-' ||
        substr(md5(p.id::text || ':' || COALESCE(p.draft_version, 1)::text || ':' || COALESCE(p.updated_at::text, p.created_at::text, NOW()::text)), 9, 4) || '-' ||
        substr(md5(p.id::text || ':' || COALESCE(p.draft_version, 1)::text || ':' || COALESCE(p.updated_at::text, p.created_at::text, NOW()::text)), 13, 4) || '-' ||
        substr(md5(p.id::text || ':' || COALESCE(p.draft_version, 1)::text || ':' || COALESCE(p.updated_at::text, p.created_at::text, NOW()::text)), 17, 4) || '-' ||
        substr(md5(p.id::text || ':' || COALESCE(p.draft_version, 1)::text || ':' || COALESCE(p.updated_at::text, p.created_at::text, NOW()::text)), 21, 12)
    )::uuid,
    p.id,
    COALESCE(p.draft_version, 1),
    COALESCE(p.draft_data, '{}'::text),
    COALESCE(p.yaml, ''::text),
    p.generator,
    COALESCE(NULLIF(p.owner_id, ''), 'system'),
    NULL,
    COALESCE(p.updated_at, p.created_at, NOW())
FROM projects p
WHERE COALESCE(p.draft_data, '') <> ''
  AND NOT EXISTS (
      SELECT 1
      FROM project_draft_versions pdv
      WHERE pdv.project_id = p.id
        AND pdv.draft_version = COALESCE(p.draft_version, 1)
  );
