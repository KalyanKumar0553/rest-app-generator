CREATE TABLE IF NOT EXISTS newsletter_subscriptions (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(320) NOT NULL UNIQUE,
    subscribed_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    welcome_email_sent BOOLEAN NOT NULL DEFAULT false,
    sending_in_progress BOOLEAN NOT NULL DEFAULT false,
    send_attempt_count INTEGER NOT NULL DEFAULT 0,
    welcome_email_sent_at TIMESTAMPTZ NULL,
    last_send_error TEXT NULL
);

CREATE INDEX IF NOT EXISTS idx_newsletter_subscriptions_pending
    ON newsletter_subscriptions (welcome_email_sent, sending_in_progress, subscribed_at);
