-- =====================================================
-- CONFIGURATION METADATA TABLES
-- =====================================================

CREATE TABLE IF NOT EXISTS config_property (
    id           BIGSERIAL PRIMARY KEY,
    category     VARCHAR(100)  NOT NULL,
    label        VARCHAR(200)  NOT NULL,
    property_key VARCHAR(300)  NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS config_property_values (
    id          BIGSERIAL PRIMARY KEY,
    property_id BIGINT        NOT NULL,
    value_key   VARCHAR(200)  NOT NULL,
    value_label VARCHAR(200)  NOT NULL,
    CONSTRAINT fk_config_property_values_property
        FOREIGN KEY (property_id)
        REFERENCES config_property (id)
        ON DELETE CASCADE
);

-- (Optional, but usually a good idea; comment out if you don’t want it)
-- ALTER TABLE config_property_values
--   ADD CONSTRAINT uq_config_property_value UNIQUE (property_id, value_key);

-- =====================================================
-- PROJECTS
-- =====================================================

CREATE TABLE IF NOT EXISTS projects (
    id                   UUID PRIMARY KEY,
    yaml                 TEXT            NOT NULL,
    artifact             VARCHAR(255),
    group_id             VARCHAR(255),
    build_tool           VARCHAR(50),
    version              VARCHAR(50),
    packaging            VARCHAR(50),
    owner_id             VARCHAR(100)    NOT NULL,
    generator            VARCHAR(100),
    name                 VARCHAR(255),
    description          VARCHAR(1000),
    spring_boot_version  VARCHAR(50),
    jdk_version          VARCHAR(20),
    include_openapi      BOOLEAN,
    angular_integration  BOOLEAN,
    created_at           TIMESTAMPTZ     NOT NULL,
    updated_at           TIMESTAMPTZ     NOT NULL,
    error_message        TEXT
);

-- (If later you add a status column to ProjectEntity,
--  you can add the index in a separate Vx migration)
-- CREATE INDEX idx_projects_status ON projects(status);

-- =====================================================
-- PROJECT RUNS
-- =====================================================

CREATE TABLE IF NOT EXISTS project_runs (
    id           UUID PRIMARY KEY,
    project_id   UUID           NOT NULL,
    owner_id     VARCHAR(100)   NOT NULL,
    type         VARCHAR(50)    NOT NULL, -- ProjectRunType (ENUM as STRING)
    status       VARCHAR(50)    NOT NULL, -- ProjectRunStatus (ENUM as STRING)
    run_number   INTEGER        NOT NULL,
    error_message TEXT,
    created_at   TIMESTAMPTZ    NOT NULL,
    updated_at   TIMESTAMPTZ,
    zip          BYTEA,
    CONSTRAINT fk_project_runs_project
        FOREIGN KEY (project_id)
        REFERENCES projects (id)
);

-- Indexes from @Table(indexes = ...)
CREATE INDEX IF NOT EXISTS idx_runs_owner_type_created
    ON project_runs (owner_id, type, created_at);

CREATE INDEX IF NOT EXISTS idx_runs_project
    ON project_runs (project_id);

-- =====================================================
-- OPTIONAL: COMMENT EXAMPLES (if you like metadata)
-- =====================================================
-- COMMENT ON TABLE config_property IS 'Configuration property definitions for UI dropdowns';
-- COMMENT ON TABLE config_property_values IS 'Allowed values for configuration properties';
-- COMMENT ON TABLE projects IS 'Uploaded YAML specs and generator metadata';
-- COMMENT ON TABLE project_runs IS 'Individual runs of the project generation workflow';
-- COMMENT ON TABLE user_info IS 'Users with auditing information';

