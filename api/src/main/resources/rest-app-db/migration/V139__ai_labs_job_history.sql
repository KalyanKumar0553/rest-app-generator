CREATE TABLE IF NOT EXISTS ai_labs_job_history (
    id uuid PRIMARY KEY,
    owner_user_id varchar(100) NOT NULL,
    status varchar(32) NOT NULL,
    generator varchar(32),
    project_id uuid,
    prompt text NOT NULL,
    steps_json text NOT NULL,
    stream_preview text,
    error_message text,
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_ai_labs_job_history_owner_created_at
    ON ai_labs_job_history (owner_user_id, created_at DESC);
