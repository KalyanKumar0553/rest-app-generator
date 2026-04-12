INSERT INTO permissions (name, display_name, description, category, active)
VALUES
    ('cdn.image.upload.read', 'View CDN Image Uploads', 'View Azure CDN image upload drafts and results', 'CDN', true),
    ('cdn.image.upload.manage', 'Manage CDN Image Uploads', 'Create Azure CDN image upload drafts', 'CDN', true),
    ('cdn.image.upload.process', 'Process CDN Image Uploads', 'Queue and process Azure CDN image uploads', 'CDN', true)
ON CONFLICT (name) DO UPDATE
SET display_name = EXCLUDED.display_name,
    description = EXCLUDED.description,
    category = EXCLUDED.category,
    active = EXCLUDED.active,
    updated_at = now();

INSERT INTO role_permissions (role_name, permission_name)
VALUES
    ('ROLE_SUPER_ADMIN', 'cdn.image.upload.read'),
    ('ROLE_SUPER_ADMIN', 'cdn.image.upload.manage'),
    ('ROLE_SUPER_ADMIN', 'cdn.image.upload.process')
ON CONFLICT DO NOTHING;

INSERT INTO routes (id, path_pattern, http_method, role_name, authority_name, priority, active)
VALUES
    ('10000000-0000-0000-0000-000000000140', '/api/v1/admin/cdn-images/**', null, 'ROLE_SUPER_ADMIN', 'cdn.image.upload.read', 45, true),
    ('10000000-0000-0000-0000-000000000141', '/api/v1/admin/cdn-images/**', null, 'ROLE_SUPER_ADMIN', 'cdn.image.upload.manage', 45, true),
    ('10000000-0000-0000-0000-000000000142', '/api/v1/admin/cdn-images/**', null, 'ROLE_SUPER_ADMIN', 'cdn.image.upload.process', 45, true)
ON CONFLICT (id) DO NOTHING;
