INSERT INTO role_permissions (role_name, permission_name)
VALUES
    ('ROLE_SUPER_ADMIN', 'config.encryption.read'),
    ('ROLE_SUPER_ADMIN', 'config.encryption.manage')
ON CONFLICT DO NOTHING;

INSERT INTO routes (id, path_pattern, http_method, role_name, authority_name, priority, active)
VALUES
    ('10000000-0000-0000-0000-000000000136', '/api/admin/data-encryption-rules/**', null, 'ROLE_SUPER_ADMIN', 'config.encryption.read', 45, true),
    ('10000000-0000-0000-0000-000000000137', '/api/admin/data-encryption-rules/**', null, 'ROLE_SUPER_ADMIN', 'config.encryption.manage', 45, true)
ON CONFLICT (id) DO NOTHING;

INSERT INTO data_encryption_rules (id, table_name, column_name, hash_shadow_column, enabled, created_at, updated_at)
SELECT
    '30000000-0000-0000-0000-000000000001'::uuid,
    'users',
    'identifier',
    'identifier_hash',
    true,
    now(),
    now()
WHERE NOT EXISTS (
    SELECT 1
    FROM data_encryption_rules
    WHERE lower(table_name) = 'users'
      AND lower(coalesce(column_name, '')) = 'identifier'
);
