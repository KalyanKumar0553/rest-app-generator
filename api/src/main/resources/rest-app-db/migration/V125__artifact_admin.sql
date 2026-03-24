CREATE TABLE IF NOT EXISTS artifact_app (
    id uuid PRIMARY KEY,
    code varchar(100) NOT NULL UNIQUE,
    name varchar(150) NOT NULL,
    description text,
    status varchar(32) NOT NULL DEFAULT 'DRAFT',
    owner_user_id varchar(100) NOT NULL,
    generator_language varchar(32) NOT NULL DEFAULT 'java',
    build_tool varchar(32) NOT NULL DEFAULT 'maven',
    enabled_packs_json text NOT NULL DEFAULT '[]',
    config_json text NOT NULL DEFAULT '{}',
    published_version varchar(64),
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_artifact_app_owner
    ON artifact_app (owner_user_id);

CREATE TABLE IF NOT EXISTS artifact_app_version (
    id uuid PRIMARY KEY,
    app_id uuid NOT NULL REFERENCES artifact_app(id) ON DELETE CASCADE,
    version_code varchar(64) NOT NULL,
    config_json text NOT NULL DEFAULT '{}',
    published boolean NOT NULL DEFAULT false,
    created_by_user_id varchar(100) NOT NULL,
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT uq_artifact_app_version UNIQUE (app_id, version_code)
);

CREATE INDEX IF NOT EXISTS idx_artifact_app_version_app
    ON artifact_app_version (app_id, created_at DESC);

INSERT INTO permissions (name, display_name, description, category, active)
VALUES
    ('artifact.app.read', 'View Artifacts', 'View artifact applications and versions', 'ARTIFACT', true),
    ('artifact.app.manage', 'Manage Artifacts', 'Create and update artifact applications and versions', 'ARTIFACT', true),
    ('artifact.app.publish', 'Publish Artifacts', 'Publish artifact application versions', 'ARTIFACT', true)
ON CONFLICT (name) DO UPDATE
SET display_name = EXCLUDED.display_name,
    description = EXCLUDED.description,
    category = EXCLUDED.category,
    active = EXCLUDED.active,
    updated_at = now();

INSERT INTO role_permissions (role_name, permission_name)
VALUES
    ('ROLE_SUPER_ADMIN', 'artifact.app.read'),
    ('ROLE_SUPER_ADMIN', 'artifact.app.manage'),
    ('ROLE_SUPER_ADMIN', 'artifact.app.publish')
ON CONFLICT DO NOTHING;

INSERT INTO routes (id, path_pattern, http_method, role_name, authority_name, priority, active)
VALUES
    ('10000000-0000-0000-0000-000000000124', '/api/v1/admin/artifacts/**', null, 'ROLE_SUPER_ADMIN', 'artifact.app.read', 45, true),
    ('10000000-0000-0000-0000-000000000125', '/api/v1/admin/artifacts/**', null, 'ROLE_SUPER_ADMIN', 'artifact.app.manage', 45, true),
    ('10000000-0000-0000-0000-000000000126', '/api/v1/admin/artifacts/**', null, 'ROLE_SUPER_ADMIN', 'artifact.app.publish', 45, true)
ON CONFLICT (id) DO NOTHING;
