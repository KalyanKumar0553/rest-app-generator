CREATE TABLE IF NOT EXISTS cdn_image_upload_draft (
    id UUID PRIMARY KEY,
    file_name VARCHAR(255) NOT NULL,
    content_type VARCHAR(150) NOT NULL,
    size_bytes BIGINT NOT NULL,
    checksum_sha256 VARCHAR(128) NOT NULL,
    binary_data BYTEA,
    status VARCHAR(32) NOT NULL,
    storage_provider VARCHAR(50) NOT NULL DEFAULT 'AZURE_CDN',
    created_by_user_id VARCHAR(100) NOT NULL,
    updated_by_user_id VARCHAR(100) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    queued_at TIMESTAMPTZ,
    processing_started_at TIMESTAMPTZ,
    completed_at TIMESTAMPTZ,
    attempt_count INTEGER NOT NULL DEFAULT 0,
    last_error_message VARCHAR(1000)
);

CREATE INDEX IF NOT EXISTS idx_cdn_image_upload_draft_status
    ON cdn_image_upload_draft (status, queued_at, created_at);

CREATE TABLE IF NOT EXISTS cdn_image_asset (
    id UUID PRIMARY KEY,
    draft_id UUID NOT NULL UNIQUE REFERENCES cdn_image_upload_draft(id) ON DELETE CASCADE,
    file_name VARCHAR(255) NOT NULL,
    content_type VARCHAR(150) NOT NULL,
    size_bytes BIGINT NOT NULL,
    checksum_sha256 VARCHAR(128) NOT NULL,
    storage_provider VARCHAR(50) NOT NULL DEFAULT 'AZURE_CDN',
    container_name VARCHAR(200) NOT NULL,
    blob_name VARCHAR(500) NOT NULL,
    image_url VARCHAR(1200) NOT NULL,
    created_by_user_id VARCHAR(100) NOT NULL,
    uploaded_by_user_id VARCHAR(100) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    uploaded_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS cdn_image_upload_settings (
    id VARCHAR(50) PRIMARY KEY,
    batch_processing_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    updated_by_user_id VARCHAR(100) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

INSERT INTO cdn_image_upload_settings (id, batch_processing_enabled, updated_by_user_id, updated_at)
VALUES ('default', TRUE, 'system', now())
ON CONFLICT (id) DO NOTHING;
