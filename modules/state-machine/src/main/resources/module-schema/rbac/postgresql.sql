CREATE TABLE IF NOT EXISTS roles (
    name TEXT PRIMARY KEY,
    type VARCHAR(32) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    display_name TEXT,
    description TEXT,
    system_role BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS permissions (
    name TEXT PRIMARY KEY,
    display_name TEXT NOT NULL,
    description TEXT,
    category VARCHAR(64) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS role_permissions (
    role_name TEXT NOT NULL REFERENCES roles(name) ON DELETE CASCADE,
    permission_name TEXT NOT NULL REFERENCES permissions(name) ON DELETE CASCADE,
    PRIMARY KEY (role_name, permission_name)
);

CREATE INDEX IF NOT EXISTS idx_role_permissions_permission
    ON role_permissions (permission_name);

CREATE TABLE IF NOT EXISTS user_roles (
    user_id TEXT NOT NULL,
    role_name TEXT NOT NULL REFERENCES roles(name) ON DELETE RESTRICT,
    PRIMARY KEY (user_id, role_name)
);

CREATE INDEX IF NOT EXISTS idx_user_roles_role_name
    ON user_roles (role_name);

CREATE TABLE IF NOT EXISTS routes (
    id UUID PRIMARY KEY,
    path_pattern VARCHAR(255) NOT NULL,
    http_method VARCHAR(10),
    authority_name TEXT,
    priority INTEGER NOT NULL DEFAULT 100,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_routes_pattern_method_authority UNIQUE (path_pattern, http_method, authority_name)
);

CREATE INDEX IF NOT EXISTS idx_routes_pattern
    ON routes (path_pattern);

CREATE INDEX IF NOT EXISTS idx_routes_authority
    ON routes (authority_name);
