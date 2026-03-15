CREATE TABLE IF NOT EXISTS invalidated_tokens (
    id text PRIMARY KEY,
    token text NOT NULL UNIQUE,
    expires_at timestamptz NOT NULL,
    created_at timestamptz NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_invalidated_tokens_expires
    ON invalidated_tokens (expires_at);

ALTER TABLE roles
    ADD COLUMN IF NOT EXISTS type varchar(32);

ALTER TABLE roles
    ADD COLUMN IF NOT EXISTS active boolean NOT NULL DEFAULT true;

UPDATE roles
SET type = CASE
    WHEN name IN ('ROLE_GUEST', 'ROLE_USER', 'ROLE_ADMIN', 'ROLE_SWAGGER_ADMIN') THEN 'AUTH_ROLE'
    ELSE 'APP_ROLE'
END
WHERE type IS NULL;

ALTER TABLE roles
    ALTER COLUMN type SET NOT NULL;

CREATE TABLE IF NOT EXISTS routes (
    id uuid PRIMARY KEY,
    path_pattern varchar(255) NOT NULL,
    http_method varchar(10),
    role_name text NOT NULL REFERENCES roles(name) ON DELETE CASCADE,
    priority integer NOT NULL DEFAULT 100,
    active boolean NOT NULL DEFAULT true,
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT uq_routes_pattern_method_role UNIQUE (path_pattern, http_method, role_name)
);

CREATE INDEX IF NOT EXISTS idx_routes_pattern
    ON routes (path_pattern);

CREATE INDEX IF NOT EXISTS idx_routes_role
    ON routes (role_name);

INSERT INTO roles (name, type, active) VALUES ('ROLE_GUEST', 'AUTH_ROLE', true)
ON CONFLICT (name) DO UPDATE SET type = EXCLUDED.type, active = EXCLUDED.active;

INSERT INTO roles (name, type, active) VALUES ('ROLE_USER', 'AUTH_ROLE', true)
ON CONFLICT (name) DO UPDATE SET type = EXCLUDED.type, active = EXCLUDED.active;

INSERT INTO roles (name, type, active) VALUES ('ROLE_ADMIN', 'AUTH_ROLE', true)
ON CONFLICT (name) DO UPDATE SET type = EXCLUDED.type, active = EXCLUDED.active;

INSERT INTO roles (name, type, active) VALUES ('ROLE_SWAGGER_ADMIN', 'AUTH_ROLE', true)
ON CONFLICT (name) DO UPDATE SET type = EXCLUDED.type, active = EXCLUDED.active;

INSERT INTO routes (id, path_pattern, http_method, role_name, priority, active)
VALUES ('10000000-0000-0000-0000-000000000001', '/api/v1/auth/roles', 'GET', 'ROLE_GUEST', 50, true)
ON CONFLICT (id) DO UPDATE
SET path_pattern = EXCLUDED.path_pattern,
    http_method = EXCLUDED.http_method,
    role_name = EXCLUDED.role_name,
    priority = EXCLUDED.priority,
    active = EXCLUDED.active,
    updated_at = now();

INSERT INTO routes (id, path_pattern, http_method, role_name, priority, active)
VALUES ('10000000-0000-0000-0000-000000000002', '/api/v1/auth/roles', 'GET', 'ROLE_USER', 50, true)
ON CONFLICT (id) DO UPDATE
SET path_pattern = EXCLUDED.path_pattern,
    http_method = EXCLUDED.http_method,
    role_name = EXCLUDED.role_name,
    priority = EXCLUDED.priority,
    active = EXCLUDED.active,
    updated_at = now();

INSERT INTO routes (id, path_pattern, http_method, role_name, priority, active)
VALUES ('10000000-0000-0000-0000-000000000003', '/api/v1/auth/roles', 'GET', 'ROLE_ADMIN', 50, true)
ON CONFLICT (id) DO UPDATE
SET path_pattern = EXCLUDED.path_pattern,
    http_method = EXCLUDED.http_method,
    role_name = EXCLUDED.role_name,
    priority = EXCLUDED.priority,
    active = EXCLUDED.active,
    updated_at = now();

INSERT INTO routes (id, path_pattern, http_method, role_name, priority, active)
VALUES ('10000000-0000-0000-0000-000000000004', '/api/v1/admin/**', null, 'ROLE_ADMIN', 40, true)
ON CONFLICT (id) DO UPDATE
SET path_pattern = EXCLUDED.path_pattern,
    http_method = EXCLUDED.http_method,
    role_name = EXCLUDED.role_name,
    priority = EXCLUDED.priority,
    active = EXCLUDED.active,
    updated_at = now();
