CREATE TABLE IF NOT EXISTS cdn_image_upload_draft (
    id uuid PRIMARY KEY,
    file_name varchar(255) NOT NULL,
    content_type varchar(150) NOT NULL,
    size_bytes bigint NOT NULL,
    checksum_sha256 varchar(128) NOT NULL,
    binary_data bytea,
    status varchar(32) NOT NULL,
    storage_provider varchar(50) NOT NULL DEFAULT 'AZURE_CDN',
    created_by_user_id varchar(100) NOT NULL,
    updated_by_user_id varchar(100) NOT NULL,
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),
    queued_at timestamptz,
    processing_started_at timestamptz,
    completed_at timestamptz,
    attempt_count integer NOT NULL DEFAULT 0,
    last_error_message varchar(1000)
);

CREATE INDEX IF NOT EXISTS idx_cdn_image_upload_draft_status
    ON cdn_image_upload_draft (status, queued_at, created_at);

CREATE TABLE IF NOT EXISTS cdn_image_asset (
    id uuid PRIMARY KEY,
    draft_id uuid NOT NULL UNIQUE REFERENCES cdn_image_upload_draft(id) ON DELETE CASCADE,
    file_name varchar(255) NOT NULL,
    content_type varchar(150) NOT NULL,
    size_bytes bigint NOT NULL,
    checksum_sha256 varchar(128) NOT NULL,
    storage_provider varchar(50) NOT NULL DEFAULT 'AZURE_CDN',
    container_name varchar(200) NOT NULL,
    blob_name varchar(500) NOT NULL,
    image_url varchar(1200) NOT NULL,
    created_by_user_id varchar(100) NOT NULL,
    uploaded_by_user_id varchar(100) NOT NULL,
    created_at timestamptz NOT NULL DEFAULT now(),
    uploaded_at timestamptz NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS cdn_image_upload_settings (
    id varchar(50) PRIMARY KEY,
    batch_processing_enabled boolean NOT NULL DEFAULT true,
    updated_by_user_id varchar(100) NOT NULL,
    updated_at timestamptz NOT NULL DEFAULT now()
);

INSERT INTO cdn_image_upload_settings (id, batch_processing_enabled, updated_by_user_id, updated_at)
VALUES ('default', true, 'system', now())
ON CONFLICT (id) DO NOTHING;
