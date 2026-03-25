ALTER TABLE plugin_module
    ADD COLUMN IF NOT EXISTS enable_config boolean NOT NULL DEFAULT false;

INSERT INTO plugin_module (
    id,
    code,
    name,
    description,
    category,
    enabled,
    enable_config,
    generator_targets_json,
    current_published_version_id,
    created_by_user_id
)
VALUES
    (
        '10000000-0000-0000-0000-000000000231',
        'rbac',
        'RBAC',
        'Built-in role-based access control module registry entry.',
        'Shipped',
        true,
        false,
        '["java","node","python"]',
        null,
        'system'
    ),
    (
        '10000000-0000-0000-0000-000000000232',
        'auth',
        'Authentication',
        'Built-in authentication module registry entry.',
        'Shipped',
        true,
        false,
        '["java","node","python"]',
        null,
        'system'
    ),
    (
        '10000000-0000-0000-0000-000000000233',
        'state-machine',
        'State Machine',
        'Built-in state-machine module registry entry.',
        'Shipped',
        true,
        false,
        '["java","node","python"]',
        null,
        'system'
    ),
    (
        '10000000-0000-0000-0000-000000000234',
        'subscription',
        'Subscription',
        'Built-in subscription module registry entry.',
        'Shipped',
        true,
        false,
        '["java","node","python"]',
        null,
        'system'
    )
ON CONFLICT (code) DO UPDATE
SET name = EXCLUDED.name,
    description = EXCLUDED.description,
    category = EXCLUDED.category,
    generator_targets_json = EXCLUDED.generator_targets_json,
    updated_at = now();
