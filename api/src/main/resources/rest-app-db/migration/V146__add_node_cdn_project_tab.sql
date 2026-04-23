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
    '20000000-0000-0000-0000-000000000051',
    'cdn',
    'CDN',
    'image',
    'module-cdn',
    95,
    'node',
    true,
    'system'
WHERE NOT EXISTS (
    SELECT 1
    FROM project_tab_definition
    WHERE lower(generator_language) = 'node'
      AND lower(tab_key) = 'cdn'
);
