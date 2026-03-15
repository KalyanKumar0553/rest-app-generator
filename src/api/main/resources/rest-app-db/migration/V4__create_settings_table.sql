CREATE TABLE IF NOT EXISTS settings (
    id uuid PRIMARY KEY,
    source text NOT NULL,
    username text,
    password text,
    hash text,
    salt text,
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_settings_source_username ON settings(source, username);
