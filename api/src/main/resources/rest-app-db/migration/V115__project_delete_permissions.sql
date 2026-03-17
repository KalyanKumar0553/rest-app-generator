INSERT INTO permissions (name, display_name, description, category, active)
VALUES
    ('project.delete', 'Delete Own Projects', 'Delete projects owned by the current user', 'PROJECT', true),
    ('project.delete.all', 'Delete All Projects', 'Delete any project in the system', 'PROJECT', true)
ON CONFLICT (name) DO UPDATE
SET display_name = EXCLUDED.display_name,
    description = EXCLUDED.description,
    category = EXCLUDED.category,
    active = EXCLUDED.active,
    updated_at = now();

INSERT INTO role_permissions (role_name, permission_name)
VALUES
    ('ROLE_GUEST', 'project.delete'),
    ('ROLE_USER', 'project.delete'),
    ('ROLE_SUPER_ADMIN', 'project.delete'),
    ('ROLE_SUPER_ADMIN', 'project.delete.all')
ON CONFLICT DO NOTHING;
