-- Agent module: sessions and messages tables for spec generation agent

CREATE TABLE IF NOT EXISTS agent_sessions (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    owner_user_id   VARCHAR(100)    NOT NULL,
    title           VARCHAR(255)    NOT NULL,
    status          VARCHAR(32)     NOT NULL DEFAULT 'ACTIVE',
    project_id      UUID,
    generated_spec  TEXT,
    error_message   TEXT,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_agent_sessions_owner ON agent_sessions (owner_user_id);
CREATE INDEX IF NOT EXISTS idx_agent_sessions_status ON agent_sessions (owner_user_id, status);

CREATE TABLE IF NOT EXISTS agent_messages (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    session_id      UUID            NOT NULL REFERENCES agent_sessions(id) ON DELETE CASCADE,
    role            VARCHAR(16)     NOT NULL,
    content         TEXT            NOT NULL,
    sequence_number INTEGER         NOT NULL,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_agent_messages_session ON agent_messages (session_id, sequence_number);
