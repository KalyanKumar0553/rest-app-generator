CREATE TABLE IF NOT EXISTS project_tab_definition (
    id uuid PRIMARY KEY,
    tab_key varchar(100) NOT NULL,
    label varchar(150) NOT NULL,
    icon varchar(100) NOT NULL,
    component_key varchar(100) NOT NULL,
    display_order integer NOT NULL,
    generator_language varchar(50) NOT NULL,
    enabled boolean NOT NULL DEFAULT true,
    created_by_user_id varchar(100) NOT NULL,
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT uq_project_tab_definition_generator_key UNIQUE (generator_language, tab_key)
);

CREATE INDEX IF NOT EXISTS idx_project_tab_definition_generator_order
    ON project_tab_definition (generator_language, display_order, tab_key);

INSERT INTO permissions (name, display_name, description, category, active)
VALUES
    ('project.tab.layout.read', 'View Project Tab Layouts', 'View language-specific project tab definitions', 'PROJECT', true),
    ('project.tab.layout.manage', 'Manage Project Tab Layouts', 'Create, update, and delete language-specific project tab definitions', 'PROJECT', true)
ON CONFLICT (name) DO UPDATE
SET display_name = EXCLUDED.display_name,
    description = EXCLUDED.description,
    category = EXCLUDED.category,
    active = EXCLUDED.active,
    updated_at = now();

INSERT INTO role_permissions (role_name, permission_name)
VALUES
    ('ROLE_SUPER_ADMIN', 'project.tab.layout.read'),
    ('ROLE_SUPER_ADMIN', 'project.tab.layout.manage')
ON CONFLICT DO NOTHING;

INSERT INTO routes (id, path_pattern, http_method, role_name, authority_name, priority, active)
VALUES
    ('10000000-0000-0000-0000-000000000134', '/api/v1/admin/project-tab-definitions/**', null, 'ROLE_SUPER_ADMIN', 'project.tab.layout.read', 45, true),
    ('10000000-0000-0000-0000-000000000135', '/api/v1/admin/project-tab-definitions/**', null, 'ROLE_SUPER_ADMIN', 'project.tab.layout.manage', 45, true)
ON CONFLICT (id) DO NOTHING;

INSERT INTO project_tab_definition (
    id, tab_key, label, icon, component_key, display_order, generator_language, enabled, created_by_user_id
)
VALUES
    ('20000000-0000-0000-0000-000000000001', 'general', 'General', 'public', 'java-general', 10, 'java', true, 'system'),
    ('20000000-0000-0000-0000-000000000002', 'actuator', 'Actuator', 'device_hub', 'actuator', 20, 'java', true, 'system'),
    ('20000000-0000-0000-0000-000000000003', 'entities', 'Entities', 'storage', 'entities', 30, 'java', true, 'system'),
    ('20000000-0000-0000-0000-000000000004', 'data-objects', 'Data Objects', 'category', 'data-objects', 40, 'java', true, 'system'),
    ('20000000-0000-0000-0000-000000000005', 'mappers', 'Mappers', 'shuffle', 'mappers', 50, 'java', true, 'system'),
    ('20000000-0000-0000-0000-000000000006', 'modules', 'Modules', 'widgets', 'modules', 60, 'java', true, 'system'),
    ('20000000-0000-0000-0000-000000000007', 'rbac', 'RBAC', 'admin_panel_settings', 'module-rbac', 70, 'java', true, 'system'),
    ('20000000-0000-0000-0000-000000000008', 'auth', 'Auth', 'lock', 'module-auth', 80, 'java', true, 'system'),
    ('20000000-0000-0000-0000-000000000009', 'state-machine', 'State Machine', 'schema', 'module-state-machine', 90, 'java', true, 'system'),
    ('20000000-0000-0000-0000-000000000010', 'subscription', 'Subscription', 'workspace_premium', 'module-subscription', 100, 'java', true, 'system'),
    ('20000000-0000-0000-0000-000000000011', 'controllers', 'Controllers', 'tune', 'controllers', 110, 'java', true, 'system'),
    ('20000000-0000-0000-0000-000000000012', 'collaborate', 'Collaborate', 'groups', 'collaborate', 120, 'java', true, 'system'),
    ('20000000-0000-0000-0000-000000000013', 'explore', 'Explore', 'search', 'explore', 130, 'java', true, 'system'),

    ('20000000-0000-0000-0000-000000000014', 'general', 'General', 'public', 'java-general', 10, 'kotlin', true, 'system'),
    ('20000000-0000-0000-0000-000000000015', 'actuator', 'Actuator', 'device_hub', 'actuator', 20, 'kotlin', true, 'system'),
    ('20000000-0000-0000-0000-000000000016', 'entities', 'Entities', 'storage', 'entities', 30, 'kotlin', true, 'system'),
    ('20000000-0000-0000-0000-000000000017', 'data-objects', 'Data Objects', 'category', 'data-objects', 40, 'kotlin', true, 'system'),
    ('20000000-0000-0000-0000-000000000018', 'mappers', 'Mappers', 'shuffle', 'mappers', 50, 'kotlin', true, 'system'),
    ('20000000-0000-0000-0000-000000000019', 'modules', 'Modules', 'widgets', 'modules', 60, 'kotlin', true, 'system'),
    ('20000000-0000-0000-0000-000000000020', 'rbac', 'RBAC', 'admin_panel_settings', 'module-rbac', 70, 'kotlin', true, 'system'),
    ('20000000-0000-0000-0000-000000000021', 'auth', 'Auth', 'lock', 'module-auth', 80, 'kotlin', true, 'system'),
    ('20000000-0000-0000-0000-000000000022', 'state-machine', 'State Machine', 'schema', 'module-state-machine', 90, 'kotlin', true, 'system'),
    ('20000000-0000-0000-0000-000000000023', 'subscription', 'Subscription', 'workspace_premium', 'module-subscription', 100, 'kotlin', true, 'system'),
    ('20000000-0000-0000-0000-000000000024', 'controllers', 'Controllers', 'tune', 'controllers', 110, 'kotlin', true, 'system'),
    ('20000000-0000-0000-0000-000000000025', 'collaborate', 'Collaborate', 'groups', 'collaborate', 120, 'kotlin', true, 'system'),
    ('20000000-0000-0000-0000-000000000026', 'explore', 'Explore', 'search', 'explore', 130, 'kotlin', true, 'system'),

    ('20000000-0000-0000-0000-000000000027', 'general', 'General', 'public', 'node-general', 10, 'node', true, 'system'),
    ('20000000-0000-0000-0000-000000000028', 'entities', 'Entities', 'storage', 'entities', 20, 'node', true, 'system'),
    ('20000000-0000-0000-0000-000000000029', 'data-objects', 'Data Objects', 'category', 'data-objects', 30, 'node', true, 'system'),
    ('20000000-0000-0000-0000-000000000030', 'mappers', 'Mappers', 'shuffle', 'mappers', 40, 'node', true, 'system'),
    ('20000000-0000-0000-0000-000000000031', 'modules', 'Modules', 'widgets', 'modules', 50, 'node', true, 'system'),
    ('20000000-0000-0000-0000-000000000032', 'rbac', 'RBAC', 'admin_panel_settings', 'module-rbac', 60, 'node', true, 'system'),
    ('20000000-0000-0000-0000-000000000033', 'auth', 'Auth', 'lock', 'module-auth', 70, 'node', true, 'system'),
    ('20000000-0000-0000-0000-000000000034', 'state-machine', 'State Machine', 'schema', 'module-state-machine', 80, 'node', true, 'system'),
    ('20000000-0000-0000-0000-000000000035', 'subscription', 'Subscription', 'workspace_premium', 'module-subscription', 90, 'node', true, 'system'),
    ('20000000-0000-0000-0000-000000000036', 'controllers', 'Controllers', 'tune', 'controllers', 100, 'node', true, 'system'),
    ('20000000-0000-0000-0000-000000000037', 'collaborate', 'Collaborate', 'groups', 'collaborate', 110, 'node', true, 'system'),
    ('20000000-0000-0000-0000-000000000038', 'explore', 'Explore', 'search', 'explore', 120, 'node', true, 'system'),

    ('20000000-0000-0000-0000-000000000039', 'general', 'General', 'public', 'node-general', 10, 'python', true, 'system'),
    ('20000000-0000-0000-0000-000000000040', 'entities', 'Entities', 'storage', 'entities', 20, 'python', true, 'system'),
    ('20000000-0000-0000-0000-000000000041', 'data-objects', 'Data Objects', 'category', 'data-objects', 30, 'python', true, 'system'),
    ('20000000-0000-0000-0000-000000000042', 'mappers', 'Mappers', 'shuffle', 'mappers', 40, 'python', true, 'system'),
    ('20000000-0000-0000-0000-000000000043', 'modules', 'Modules', 'widgets', 'modules', 50, 'python', true, 'system'),
    ('20000000-0000-0000-0000-000000000044', 'rbac', 'RBAC', 'admin_panel_settings', 'module-rbac', 60, 'python', true, 'system'),
    ('20000000-0000-0000-0000-000000000045', 'auth', 'Auth', 'lock', 'module-auth', 70, 'python', true, 'system'),
    ('20000000-0000-0000-0000-000000000046', 'state-machine', 'State Machine', 'schema', 'module-state-machine', 80, 'python', true, 'system'),
    ('20000000-0000-0000-0000-000000000047', 'subscription', 'Subscription', 'workspace_premium', 'module-subscription', 90, 'python', true, 'system'),
    ('20000000-0000-0000-0000-000000000048', 'controllers', 'Controllers', 'tune', 'controllers', 100, 'python', true, 'system'),
    ('20000000-0000-0000-0000-000000000049', 'collaborate', 'Collaborate', 'groups', 'collaborate', 110, 'python', true, 'system'),
    ('20000000-0000-0000-0000-000000000050', 'explore', 'Explore', 'search', 'explore', 120, 'python', true, 'system')
ON CONFLICT (generator_language, tab_key) DO UPDATE
SET label = EXCLUDED.label,
    icon = EXCLUDED.icon,
    component_key = EXCLUDED.component_key,
    display_order = EXCLUDED.display_order,
    enabled = EXCLUDED.enabled,
    updated_at = now();
