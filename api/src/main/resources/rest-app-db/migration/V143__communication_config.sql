CREATE TABLE IF NOT EXISTS communication_config (
    id UUID PRIMARY KEY,
    service_type VARCHAR(50) NOT NULL UNIQUE,
    enabled BOOLEAN NOT NULL DEFAULT false,
    display_name VARCHAR(200) NOT NULL,
    endpoint VARCHAR(500),
    sender_id VARCHAR(200),
    channel_registration_id VARCHAR(200),
    connection_string_encrypted VARCHAR(4000),
    connection_string_hash VARCHAR(128),
    connection_string_salt VARCHAR(128),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

INSERT INTO communication_config (
    id, service_type, enabled, display_name, endpoint, sender_id, channel_registration_id, connection_string_encrypted, connection_string_hash, connection_string_salt
)
VALUES
    ('10000000-0000-0000-0000-000000000143', 'WHATSAPP', true, 'WhatsApp Azure Communication Service', NULL, NULL, NULL, NULL, NULL, NULL),
    ('10000000-0000-0000-0000-000000000144', 'EMAIL', false, 'Email Azure Communication Service', NULL, NULL, NULL, NULL, NULL, NULL),
    ('10000000-0000-0000-0000-000000000145', 'SMS', false, 'SMS Communication Service', NULL, NULL, NULL, NULL, NULL, NULL)
ON CONFLICT (service_type) DO UPDATE
SET display_name = EXCLUDED.display_name,
    enabled = EXCLUDED.enabled,
    updated_at = now();
