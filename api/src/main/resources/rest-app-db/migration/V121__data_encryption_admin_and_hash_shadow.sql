ALTER TABLE data_encryption_rules
    ADD COLUMN IF NOT EXISTS hash_shadow_column VARCHAR(150);

ALTER TABLE users
    ADD COLUMN IF NOT EXISTS identifier_hash VARCHAR(64);

CREATE UNIQUE INDEX IF NOT EXISTS uq_users_identifier_hash
    ON users (identifier_hash)
    WHERE identifier_hash IS NOT NULL;

INSERT INTO permissions (name, display_name, description, category, active)
VALUES
    ('config.encryption.read', 'View Data Encryption Rules', 'View configured data encryption rules', 'CONFIG', true),
    ('config.encryption.manage', 'Manage Data Encryption Rules', 'Create and update data encryption rules', 'CONFIG', true)
ON CONFLICT (name) DO UPDATE
SET display_name = EXCLUDED.display_name,
    description = EXCLUDED.description,
    category = EXCLUDED.category,
    active = EXCLUDED.active;

INSERT INTO role_permissions (role_name, permission_name)
VALUES
    ('ROLE_SUPER_ADMIN', 'config.encryption.read'),
    ('ROLE_SUPER_ADMIN', 'config.encryption.manage')
ON CONFLICT DO NOTHING;
