CREATE TABLE IF NOT EXISTS plugin_module (
    id uuid PRIMARY KEY,
    code varchar(100) NOT NULL UNIQUE,
    name varchar(150) NOT NULL,
    description text,
    category varchar(100),
    enabled boolean NOT NULL DEFAULT true,
    generator_targets_json text NOT NULL DEFAULT '["java"]',
    current_published_version_id uuid,
    created_by_user_id varchar(100) NOT NULL,
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS plugin_module_version (
    id uuid PRIMARY KEY,
    plugin_module_id uuid NOT NULL REFERENCES plugin_module(id) ON DELETE CASCADE,
    version_code varchar(64) NOT NULL,
    changelog text,
    file_name varchar(255) NOT NULL,
    storage_key varchar(500) NOT NULL UNIQUE,
    checksum_sha256 varchar(128) NOT NULL,
    size_bytes bigint NOT NULL,
    published boolean NOT NULL DEFAULT false,
    created_by_user_id varchar(100) NOT NULL,
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT uq_plugin_module_version UNIQUE (plugin_module_id, version_code)
);

CREATE INDEX IF NOT EXISTS idx_plugin_module_enabled
    ON plugin_module (enabled, name);

CREATE INDEX IF NOT EXISTS idx_plugin_module_version_module
    ON plugin_module_version (plugin_module_id, created_at DESC);

ALTER TABLE plugin_module
    ADD CONSTRAINT fk_plugin_module_current_published_version
    FOREIGN KEY (current_published_version_id) REFERENCES plugin_module_version(id);

INSERT INTO permissions (name, display_name, description, category, active)
VALUES
    ('config.feature.read', 'View Feature Controls', 'View configurable feature flags', 'CONFIG', true),
    ('config.feature.manage', 'Manage Feature Controls', 'Update configurable feature flags', 'CONFIG', true),
    ('plugin.module.read', 'View Plugin Modules', 'View plugin modules and versions', 'PLUGIN', true),
    ('plugin.module.manage', 'Manage Plugin Modules', 'Create and update plugin modules and versions', 'PLUGIN', true),
    ('plugin.module.publish', 'Publish Plugin Modules', 'Publish plugin module versions', 'PLUGIN', true)
ON CONFLICT (name) DO UPDATE
SET display_name = EXCLUDED.display_name,
    description = EXCLUDED.description,
    category = EXCLUDED.category,
    active = EXCLUDED.active,
    updated_at = now();

INSERT INTO role_permissions (role_name, permission_name)
VALUES
    ('ROLE_SUPER_ADMIN', 'config.feature.read'),
    ('ROLE_SUPER_ADMIN', 'config.feature.manage'),
    ('ROLE_SUPER_ADMIN', 'plugin.module.read'),
    ('ROLE_SUPER_ADMIN', 'plugin.module.manage'),
    ('ROLE_SUPER_ADMIN', 'plugin.module.publish')
ON CONFLICT DO NOTHING;

INSERT INTO routes (id, path_pattern, http_method, role_name, authority_name, priority, active)
VALUES
    ('10000000-0000-0000-0000-000000000129', '/api/config/features', null, 'ROLE_SUPER_ADMIN', 'config.feature.read', 45, true),
    ('10000000-0000-0000-0000-000000000130', '/api/config/features/**', null, 'ROLE_SUPER_ADMIN', 'config.feature.manage', 45, true),
    ('10000000-0000-0000-0000-000000000131', '/api/v1/admin/plugin-modules/**', null, 'ROLE_SUPER_ADMIN', 'plugin.module.read', 45, true),
    ('10000000-0000-0000-0000-000000000132', '/api/v1/admin/plugin-modules/**', null, 'ROLE_SUPER_ADMIN', 'plugin.module.manage', 45, true),
    ('10000000-0000-0000-0000-000000000133', '/api/v1/admin/plugin-modules/**', null, 'ROLE_SUPER_ADMIN', 'plugin.module.publish', 45, true)
ON CONFLICT (id) DO NOTHING;

INSERT INTO config_property (category, label, property_key, current_value_key)
VALUES ('FEATURES', 'Plugin Modules', 'app.feature.plugin-modules.enabled', 'false')
ON CONFLICT (property_key) DO UPDATE
SET category = EXCLUDED.category,
    label = EXCLUDED.label;

DELETE FROM config_property_values
WHERE property_id = (SELECT id FROM config_property WHERE property_key = 'app.feature.plugin-modules.enabled');

INSERT INTO config_property_values (property_id, value_key, value_label)
VALUES
((SELECT id FROM config_property WHERE property_key = 'app.feature.plugin-modules.enabled'), 'true', 'Enabled'),
((SELECT id FROM config_property WHERE property_key = 'app.feature.plugin-modules.enabled'), 'false', 'Disabled');
