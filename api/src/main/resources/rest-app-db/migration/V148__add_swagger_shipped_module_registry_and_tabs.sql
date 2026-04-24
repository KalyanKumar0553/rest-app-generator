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
SELECT
    '10000000-0000-0000-0000-000000000235',
    'swagger',
    'Swagger',
    'Built-in Swagger/OpenAPI module registry entry.',
    'Shipped',
    true,
    false,
    '["java","kotlin","node","python"]',
    null,
    'system'
WHERE NOT EXISTS (
    SELECT 1
    FROM plugin_module
    WHERE lower(code) = 'swagger'
);

INSERT INTO project_tab_definition (
    id,
    tab_key,
    label,
    icon,
    component_key,
    display_order,
    generator_language,
    enabled,
    created_by_user_id
)
SELECT
    '20000000-0000-0000-0000-000000000053',
    'swagger',
    'Swagger',
    'menu_book',
    'module-swagger',
    105,
    'java',
    true,
    'system'
WHERE NOT EXISTS (
    SELECT 1
    FROM project_tab_definition
    WHERE lower(generator_language) = 'java'
      AND lower(tab_key) = 'swagger'
);

INSERT INTO project_tab_definition (
    id,
    tab_key,
    label,
    icon,
    component_key,
    display_order,
    generator_language,
    enabled,
    created_by_user_id
)
SELECT
    '20000000-0000-0000-0000-000000000054',
    'swagger',
    'Swagger',
    'menu_book',
    'module-swagger',
    105,
    'kotlin',
    true,
    'system'
WHERE NOT EXISTS (
    SELECT 1
    FROM project_tab_definition
    WHERE lower(generator_language) = 'kotlin'
      AND lower(tab_key) = 'swagger'
);

INSERT INTO project_tab_definition (
    id,
    tab_key,
    label,
    icon,
    component_key,
    display_order,
    generator_language,
    enabled,
    created_by_user_id
)
SELECT
    '20000000-0000-0000-0000-000000000055',
    'swagger',
    'Swagger',
    'menu_book',
    'module-swagger',
    92,
    'node',
    true,
    'system'
WHERE NOT EXISTS (
    SELECT 1
    FROM project_tab_definition
    WHERE lower(generator_language) = 'node'
      AND lower(tab_key) = 'swagger'
);

INSERT INTO project_tab_definition (
    id,
    tab_key,
    label,
    icon,
    component_key,
    display_order,
    generator_language,
    enabled,
    created_by_user_id
)
SELECT
    '20000000-0000-0000-0000-000000000056',
    'swagger',
    'Swagger',
    'menu_book',
    'module-swagger',
    92,
    'python',
    true,
    'system'
WHERE NOT EXISTS (
    SELECT 1
    FROM project_tab_definition
    WHERE lower(generator_language) = 'python'
      AND lower(tab_key) = 'swagger'
);
