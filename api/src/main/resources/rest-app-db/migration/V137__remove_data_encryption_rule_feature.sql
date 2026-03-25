DELETE FROM routes
WHERE path_pattern = '/api/admin/data-encryption-rules/**';

DELETE FROM role_permissions
WHERE permission_name IN ('config.encryption.read', 'config.encryption.manage');

DELETE FROM permissions
WHERE name IN ('config.encryption.read', 'config.encryption.manage');

DROP TABLE IF EXISTS data_encryption_rule_shadow_columns;
DROP TABLE IF EXISTS data_encryption_rules;

DROP INDEX IF EXISTS uq_users_identifier_hash;

ALTER TABLE users
    DROP COLUMN IF EXISTS identifier_hash;
