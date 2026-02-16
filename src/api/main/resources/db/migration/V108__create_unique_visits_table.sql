CREATE TABLE IF NOT EXISTS unique_visits (
    id BIGSERIAL PRIMARY KEY,
    ip_hash VARCHAR(64) NOT NULL UNIQUE,
    first_seen_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    last_seen_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    hit_count BIGINT NOT NULL DEFAULT 1
);

CREATE INDEX IF NOT EXISTS idx_unique_visits_last_seen_at
    ON unique_visits (last_seen_at DESC);
