CREATE TABLE IF NOT EXISTS payment_configs (
    id UUID PRIMARY KEY,
    provider_type VARCHAR(32) NOT NULL UNIQUE,
    enabled BOOLEAN NOT NULL DEFAULT false,
    is_default BOOLEAN NOT NULL DEFAULT false,
    merchant_id VARCHAR(200),
    public_key VARCHAR(200),
    secret_key_encrypted VARCHAR(4000),
    secret_key_hash VARCHAR(128),
    secret_key_salt VARCHAR(128),
    webhook_secret_encrypted VARCHAR(4000),
    webhook_secret_hash VARCHAR(128),
    webhook_secret_salt VARCHAR(128),
    endpoint_url VARCHAR(500),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS orders (
    id UUID PRIMARY KEY,
    guest_token VARCHAR(128) NOT NULL,
    order_reference VARCHAR(64) NOT NULL UNIQUE,
    currency_code VARCHAR(10) NOT NULL,
    amount NUMERIC(19,2) NOT NULL,
    status VARCHAR(32) NOT NULL,
    payment_provider VARCHAR(32),
    payment_reference VARCHAR(128),
    payment_status VARCHAR(32),
    payment_attempt_count INT NOT NULL DEFAULT 0,
    payment_last_checked_at TIMESTAMPTZ,
    payment_completed_at TIMESTAMPTZ,
    payment_failure_reason VARCHAR(500),
    payload_json TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS payments (
    id UUID PRIMARY KEY,
    order_id UUID NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    provider_type VARCHAR(32) NOT NULL,
    status VARCHAR(32) NOT NULL,
    provider_reference VARCHAR(128),
    provider_payment_id VARCHAR(128),
    provider_order_id VARCHAR(128),
    provider_signature_hash VARCHAR(128),
    status_reason VARCHAR(500),
    retry_count INT NOT NULL DEFAULT 0,
    next_poll_at TIMESTAMPTZ,
    last_polled_at TIMESTAMPTZ,
    raw_response_json TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

INSERT INTO payment_configs (id, provider_type, enabled, is_default, merchant_id, public_key, endpoint_url)
VALUES
    ('10000000-0000-0000-0000-000000001441', 'RAZORPAY', FALSE, FALSE, NULL, NULL, NULL),
    ('10000000-0000-0000-0000-000000001442', 'PAYPAL', FALSE, FALSE, NULL, NULL, NULL),
    ('10000000-0000-0000-0000-000000001443', 'STRIPE', FALSE, FALSE, NULL, NULL, NULL),
    ('10000000-0000-0000-0000-000000001444', 'PHONEPE', TRUE, TRUE, NULL, NULL, NULL),
    ('10000000-0000-0000-0000-000000001445', 'PAYTM', FALSE, FALSE, NULL, NULL, NULL)
ON CONFLICT (provider_type) DO UPDATE
SET enabled = EXCLUDED.enabled,
    is_default = EXCLUDED.is_default,
    updated_at = now();
