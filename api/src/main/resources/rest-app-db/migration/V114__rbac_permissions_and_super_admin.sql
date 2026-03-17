ALTER TABLE roles
    ADD COLUMN IF NOT EXISTS display_name text,
    ADD COLUMN IF NOT EXISTS description text,
    ADD COLUMN IF NOT EXISTS system_role boolean NOT NULL DEFAULT false;

CREATE TABLE IF NOT EXISTS permissions (
    name text PRIMARY KEY,
    display_name text NOT NULL,
    description text,
    category varchar(64) NOT NULL,
    active boolean NOT NULL DEFAULT true,
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS role_permissions (
    role_name text NOT NULL REFERENCES roles(name) ON DELETE CASCADE,
    permission_name text NOT NULL REFERENCES permissions(name) ON DELETE CASCADE,
    PRIMARY KEY (role_name, permission_name)
);

CREATE INDEX IF NOT EXISTS idx_role_permissions_permission
    ON role_permissions (permission_name);

ALTER TABLE routes
    ADD COLUMN IF NOT EXISTS authority_name text;

INSERT INTO roles (name, type, active, display_name, description, system_role)
VALUES ('ROLE_SUPER_ADMIN', 'AUTH_ROLE', true, 'Super Admin', 'Full platform administration access', true)
ON CONFLICT (name) DO UPDATE
SET type = EXCLUDED.type,
    active = EXCLUDED.active,
    display_name = EXCLUDED.display_name,
    description = EXCLUDED.description,
    system_role = EXCLUDED.system_role;

UPDATE user_roles
SET role_name = 'ROLE_SUPER_ADMIN'
WHERE role_name = 'ROLE_ADMIN';

UPDATE routes
SET role_name = 'ROLE_SUPER_ADMIN',
    updated_at = now()
WHERE role_name = 'ROLE_ADMIN';

DELETE FROM roles
WHERE name = 'ROLE_ADMIN';

INSERT INTO roles (name, type, active, display_name, description, system_role)
VALUES
    ('ROLE_GUEST', 'AUTH_ROLE', true, 'Guest', 'Default authenticated app access', true),
    ('ROLE_USER', 'AUTH_ROLE', true, 'User', 'Standard authenticated app access', true),
    ('ROLE_SWAGGER_ADMIN', 'AUTH_ROLE', true, 'Swagger Admin', 'Swagger administration access', true)
ON CONFLICT (name) DO UPDATE
SET type = EXCLUDED.type,
    active = EXCLUDED.active,
    display_name = EXCLUDED.display_name,
    description = EXCLUDED.description,
    system_role = EXCLUDED.system_role;

INSERT INTO permissions (name, display_name, description, category, active)
VALUES
    ('auth.roles.read', 'View Access Profile', 'View the current user role and permission assignments', 'AUTH', true),
    ('project.create', 'Create Projects', 'Create new projects', 'PROJECT', true),
    ('project.read', 'View Owned And Shared Projects', 'View projects the user owns or is shared on', 'PROJECT', true),
    ('project.update', 'Update Owned And Shared Projects', 'Update project specs for projects the user can edit', 'PROJECT', true),
    ('project.generate', 'Generate Project Code', 'Trigger project generation for projects the user can edit', 'PROJECT', true),
    ('project.contributor.manage', 'Manage Own Project Contributors', 'Manage contributors for projects owned by the user', 'PROJECT', true),
    ('project.read.all', 'View All Projects', 'View every project in the system', 'PROJECT', true),
    ('project.update.all', 'Update All Projects', 'Update any project in the system', 'PROJECT', true),
    ('project.generate.all', 'Generate For All Projects', 'Generate code for any project in the system', 'PROJECT', true),
    ('project.contributor.manage.all', 'Manage All Project Contributors', 'Manage contributors on any project', 'PROJECT', true),
    ('config.reload', 'Reload Config Metadata', 'Reload configuration metadata defaults', 'CONFIG', true),
    ('rbac.role.read', 'View Roles', 'View role and permission assignments', 'RBAC', true),
    ('rbac.role.manage', 'Manage Roles', 'Create, update, activate, and deactivate roles', 'RBAC', true),
    ('rbac.user-role.manage', 'Manage User Roles', 'Assign roles to users', 'RBAC', true),
    ('swagger.password.manage', 'Manage Swagger Credentials', 'Reset Swagger credentials', 'SWAGGER', true)
ON CONFLICT (name) DO UPDATE
SET display_name = EXCLUDED.display_name,
    description = EXCLUDED.description,
    category = EXCLUDED.category,
    active = EXCLUDED.active,
    updated_at = now();

INSERT INTO role_permissions (role_name, permission_name)
VALUES
    ('ROLE_GUEST', 'auth.roles.read'),
    ('ROLE_GUEST', 'project.create'),
    ('ROLE_GUEST', 'project.read'),
    ('ROLE_GUEST', 'project.update'),
    ('ROLE_GUEST', 'project.generate'),
    ('ROLE_GUEST', 'project.contributor.manage'),
    ('ROLE_USER', 'auth.roles.read'),
    ('ROLE_USER', 'project.create'),
    ('ROLE_USER', 'project.read'),
    ('ROLE_USER', 'project.update'),
    ('ROLE_USER', 'project.generate'),
    ('ROLE_USER', 'project.contributor.manage'),
    ('ROLE_SUPER_ADMIN', 'auth.roles.read'),
    ('ROLE_SUPER_ADMIN', 'project.create'),
    ('ROLE_SUPER_ADMIN', 'project.read'),
    ('ROLE_SUPER_ADMIN', 'project.update'),
    ('ROLE_SUPER_ADMIN', 'project.generate'),
    ('ROLE_SUPER_ADMIN', 'project.contributor.manage'),
    ('ROLE_SUPER_ADMIN', 'project.read.all'),
    ('ROLE_SUPER_ADMIN', 'project.update.all'),
    ('ROLE_SUPER_ADMIN', 'project.generate.all'),
    ('ROLE_SUPER_ADMIN', 'project.contributor.manage.all'),
    ('ROLE_SUPER_ADMIN', 'config.reload'),
    ('ROLE_SUPER_ADMIN', 'rbac.role.read'),
    ('ROLE_SUPER_ADMIN', 'rbac.role.manage'),
    ('ROLE_SUPER_ADMIN', 'rbac.user-role.manage'),
    ('ROLE_SWAGGER_ADMIN', 'swagger.password.manage')
ON CONFLICT DO NOTHING;

UPDATE routes
SET path_pattern = '/api/v1/admin/rbac/**',
    role_name = 'ROLE_SUPER_ADMIN',
    authority_name = 'rbac.role.read',
    updated_at = now()
WHERE id = '10000000-0000-0000-0000-000000000004';

UPDATE routes
SET authority_name = 'auth.roles.read',
    updated_at = now()
WHERE path_pattern = '/api/v1/auth/roles'
  AND http_method = 'GET';
