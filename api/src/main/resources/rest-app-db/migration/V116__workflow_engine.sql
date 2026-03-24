CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE IF NOT EXISTS workflow_executor_pools (
    id UUID PRIMARY KEY,
    pool_code VARCHAR(120) NOT NULL UNIQUE,
    pool_name VARCHAR(200) NOT NULL,
    core_pool_size INTEGER NOT NULL,
    max_pool_size INTEGER NOT NULL,
    queue_capacity INTEGER NOT NULL,
    keep_alive_seconds INTEGER NOT NULL,
    active BOOLEAN NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE IF NOT EXISTS workflow_definitions (
    id UUID PRIMARY KEY,
    code VARCHAR(120) NOT NULL,
    name VARCHAR(200) NOT NULL,
    language VARCHAR(50) NOT NULL,
    version INTEGER NOT NULL,
    active BOOLEAN NOT NULL,
    dispatch_pool_code VARCHAR(120) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uq_workflow_definition_code_version UNIQUE (code, version),
    CONSTRAINT fk_workflow_definition_dispatch_pool
        FOREIGN KEY (dispatch_pool_code)
        REFERENCES workflow_executor_pools (pool_code)
);

CREATE TABLE IF NOT EXISTS workflow_steps (
    id UUID PRIMARY KEY,
    workflow_id UUID NOT NULL,
    step_code VARCHAR(120) NOT NULL,
    step_name VARCHAR(200) NOT NULL,
    executor_key VARCHAR(120) NOT NULL,
    enabled BOOLEAN NOT NULL,
    terminal BOOLEAN NOT NULL,
    step_order INTEGER NOT NULL,
    pool_code VARCHAR(120) NOT NULL,
    async_execution BOOLEAN NOT NULL,
    timeout_ms BIGINT,
    run_condition_json TEXT,
    required_inputs_json TEXT,
    optional_inputs_json TEXT,
    declared_outputs_json TEXT,
    retry_enabled BOOLEAN NOT NULL,
    retry_max_attempts INTEGER,
    retry_backoff_ms BIGINT,
    retry_backoff_multiplier DOUBLE PRECISION,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_workflow_steps_definition
        FOREIGN KEY (workflow_id)
        REFERENCES workflow_definitions (id)
        ON DELETE CASCADE,
    CONSTRAINT fk_workflow_steps_pool
        FOREIGN KEY (pool_code)
        REFERENCES workflow_executor_pools (pool_code),
    CONSTRAINT uq_workflow_step_code UNIQUE (workflow_id, step_code),
    CONSTRAINT uq_workflow_step_order UNIQUE (workflow_id, step_order)
);

CREATE TABLE IF NOT EXISTS workflow_transitions (
    id UUID PRIMARY KEY,
    workflow_step_id UUID NOT NULL,
    transition_type VARCHAR(30) NOT NULL,
    target_step_code VARCHAR(120) NOT NULL,
    condition_json TEXT,
    priority INTEGER NOT NULL,
    CONSTRAINT fk_workflow_transitions_step
        FOREIGN KEY (workflow_step_id)
        REFERENCES workflow_steps (id)
        ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_workflow_definition_language_active
    ON workflow_definitions (language, active);

CREATE INDEX IF NOT EXISTS idx_workflow_step_workflow_order
    ON workflow_steps (workflow_id, step_order);

CREATE INDEX IF NOT EXISTS idx_workflow_step_workflow_code
    ON workflow_steps (workflow_id, step_code);

CREATE INDEX IF NOT EXISTS idx_workflow_transition_step_type_priority
    ON workflow_transitions (workflow_step_id, transition_type, priority);

INSERT INTO workflow_executor_pools (
    id, pool_code, pool_name, core_pool_size, max_pool_size, queue_capacity, keep_alive_seconds, active, created_at, updated_at
)
SELECT gen_random_uuid(), 'workflow-dispatch', 'Workflow Dispatch Pool', 4, 8, 100, 60, TRUE, now(), now()
WHERE NOT EXISTS (
    SELECT 1 FROM workflow_executor_pools WHERE pool_code = 'workflow-dispatch'
);

INSERT INTO workflow_executor_pools (
    id, pool_code, pool_name, core_pool_size, max_pool_size, queue_capacity, keep_alive_seconds, active, created_at, updated_at
)
SELECT gen_random_uuid(), 'workflow-default', 'Workflow Default Pool', 4, 8, 200, 60, TRUE, now(), now()
WHERE NOT EXISTS (
    SELECT 1 FROM workflow_executor_pools WHERE pool_code = 'workflow-default'
);

INSERT INTO workflow_executor_pools (
    id, pool_code, pool_name, core_pool_size, max_pool_size, queue_capacity, keep_alive_seconds, active, created_at, updated_at
)
SELECT gen_random_uuid(), 'workflow-io', 'Workflow IO Pool', 4, 8, 200, 60, TRUE, now(), now()
WHERE NOT EXISTS (
    SELECT 1 FROM workflow_executor_pools WHERE pool_code = 'workflow-io'
);

INSERT INTO workflow_definitions (
    id, code, name, language, version, active, dispatch_pool_code, created_at, updated_at
)
SELECT gen_random_uuid(), 'JAVA_DEFAULT', 'Java Workflow', 'JAVA', 1, TRUE, 'workflow-dispatch', now(), now()
WHERE NOT EXISTS (
    SELECT 1 FROM workflow_definitions WHERE code = 'JAVA_DEFAULT' AND version = 1
);

INSERT INTO workflow_definitions (
    id, code, name, language, version, active, dispatch_pool_code, created_at, updated_at
)
SELECT gen_random_uuid(), 'KOTLIN_DEFAULT', 'Kotlin Workflow', 'KOTLIN', 1, TRUE, 'workflow-dispatch', now(), now()
WHERE NOT EXISTS (
    SELECT 1 FROM workflow_definitions WHERE code = 'KOTLIN_DEFAULT' AND version = 1
);

INSERT INTO workflow_definitions (
    id, code, name, language, version, active, dispatch_pool_code, created_at, updated_at
)
SELECT gen_random_uuid(), 'NODE_DEFAULT', 'Node Workflow', 'NODE', 1, TRUE, 'workflow-dispatch', now(), now()
WHERE NOT EXISTS (
    SELECT 1 FROM workflow_definitions WHERE code = 'NODE_DEFAULT' AND version = 1
);

INSERT INTO workflow_definitions (
    id, code, name, language, version, active, dispatch_pool_code, created_at, updated_at
)
SELECT gen_random_uuid(), 'PYTHON_DEFAULT', 'Python Workflow', 'PYTHON', 1, TRUE, 'workflow-dispatch', now(), now()
WHERE NOT EXISTS (
    SELECT 1 FROM workflow_definitions WHERE code = 'PYTHON_DEFAULT' AND version = 1
);

INSERT INTO workflow_steps (
    id, workflow_id, step_code, step_name, executor_key, enabled, terminal, step_order, pool_code, async_execution,
    timeout_ms, run_condition_json, required_inputs_json, optional_inputs_json, declared_outputs_json,
    retry_enabled, retry_max_attempts, retry_backoff_ms, retry_backoff_multiplier, created_at, updated_at
)
SELECT gen_random_uuid(), wd.id, seed.step_code, seed.step_name, seed.executor_key, TRUE, seed.terminal, seed.step_order,
       seed.pool_code, FALSE, 300000, NULL, seed.required_inputs_json, '[]', '["status"]', TRUE, 2, 500, 2.0, now(), now()
FROM workflow_definitions wd
JOIN (
    VALUES
        ('JAVA_DEFAULT', 1, 'DTO_GENERATION', 'DTO Generation', 'dtoGenerationExecutor', FALSE, 'workflow-default', '["rootDir","yaml","groupId","artifactId"]'),
        ('JAVA_DEFAULT', 2, 'ENUM_GENERATION', 'Enum Generation', 'enumGenerationExecutor', FALSE, 'workflow-default', '["rootDir","yaml","groupId","artifactId"]'),
        ('JAVA_DEFAULT', 3, 'MODEL_GENERATION', 'Model Generation', 'modelGenerationExecutor', FALSE, 'workflow-default', '["rootDir","yaml","groupId","artifactId"]'),
        ('JAVA_DEFAULT', 4, 'SWAGGER_GENERATION', 'Swagger Generation', 'swaggerGenerationExecutor', FALSE, 'workflow-default', '["rootDir","yaml","groupId","artifactId"]'),
        ('JAVA_DEFAULT', 5, 'REST_GENERATION', 'REST Generation', 'restGenerationExecutor', FALSE, 'workflow-default', '["rootDir","yaml","groupId","artifactId"]'),
        ('JAVA_DEFAULT', 6, 'CRUD_GENERATION', 'CRUD Generation', 'crudGenerationExecutor', FALSE, 'workflow-default', '["rootDir","yaml","groupId","artifactId"]'),
        ('JAVA_DEFAULT', 7, 'APPLICATION_FILES', 'Application Files', 'applicationFileGenerationExecutor', FALSE, 'workflow-default', '["rootDir","yaml","groupId","artifactId"]'),
        ('JAVA_DEFAULT', 8, 'ACTUATOR_CONFIGURATION', 'Actuator Configuration', 'actuatorConfigurationExecutor', FALSE, 'workflow-default', '["rootDir","yaml","groupId","artifactId"]'),
        ('JAVA_DEFAULT', 9, 'EXCEPTION_PACKAGE_GENERATION', 'Exception Package Generation', 'exceptionPackageGenerationExecutor', FALSE, 'workflow-default', '["rootDir","yaml","groupId","artifactId"]'),
        ('JAVA_DEFAULT', 10, 'MAPPER_GENERATION', 'Mapper Generation', 'mapperGenerationExecutor', FALSE, 'workflow-default', '["rootDir","yaml","groupId","artifactId"]'),
        ('JAVA_DEFAULT', 11, 'DOCKER_GENERATION', 'Docker Generation', 'dockerGenerationExecutor', FALSE, 'workflow-default', '["rootDir","yaml","groupId","artifactId"]'),
        ('JAVA_DEFAULT', 12, 'SCAFFOLD', 'Scaffold', 'scaffoldExecutor', TRUE, 'workflow-io', '["rootDir","yaml","groupId","artifactId"]'),
        ('KOTLIN_DEFAULT', 1, 'DTO_GENERATION', 'DTO Generation', 'dtoGenerationExecutor', FALSE, 'workflow-default', '["rootDir","yaml","groupId","artifactId"]'),
        ('KOTLIN_DEFAULT', 2, 'ENUM_GENERATION', 'Enum Generation', 'enumGenerationExecutor', FALSE, 'workflow-default', '["rootDir","yaml","groupId","artifactId"]'),
        ('KOTLIN_DEFAULT', 3, 'MODEL_GENERATION', 'Model Generation', 'modelGenerationExecutor', FALSE, 'workflow-default', '["rootDir","yaml","groupId","artifactId"]'),
        ('KOTLIN_DEFAULT', 4, 'SWAGGER_GENERATION', 'Swagger Generation', 'swaggerGenerationExecutor', FALSE, 'workflow-default', '["rootDir","yaml","groupId","artifactId"]'),
        ('KOTLIN_DEFAULT', 5, 'REST_GENERATION', 'REST Generation', 'restGenerationExecutor', FALSE, 'workflow-default', '["rootDir","yaml","groupId","artifactId"]'),
        ('KOTLIN_DEFAULT', 6, 'CRUD_GENERATION', 'CRUD Generation', 'crudGenerationExecutor', FALSE, 'workflow-default', '["rootDir","yaml","groupId","artifactId"]'),
        ('KOTLIN_DEFAULT', 7, 'APPLICATION_FILES', 'Application Files', 'applicationFileGenerationExecutor', FALSE, 'workflow-default', '["rootDir","yaml","groupId","artifactId"]'),
        ('KOTLIN_DEFAULT', 8, 'ACTUATOR_CONFIGURATION', 'Actuator Configuration', 'actuatorConfigurationExecutor', FALSE, 'workflow-default', '["rootDir","yaml","groupId","artifactId"]'),
        ('KOTLIN_DEFAULT', 9, 'EXCEPTION_PACKAGE_GENERATION', 'Exception Package Generation', 'exceptionPackageGenerationExecutor', FALSE, 'workflow-default', '["rootDir","yaml","groupId","artifactId"]'),
        ('KOTLIN_DEFAULT', 10, 'MAPPER_GENERATION', 'Mapper Generation', 'mapperGenerationExecutor', FALSE, 'workflow-default', '["rootDir","yaml","groupId","artifactId"]'),
        ('KOTLIN_DEFAULT', 11, 'DOCKER_GENERATION', 'Docker Generation', 'dockerGenerationExecutor', FALSE, 'workflow-default', '["rootDir","yaml","groupId","artifactId"]'),
        ('KOTLIN_DEFAULT', 12, 'SCAFFOLD', 'Scaffold', 'scaffoldExecutor', TRUE, 'workflow-io', '["rootDir","yaml","groupId","artifactId"]'),
        ('NODE_DEFAULT', 1, 'SCAFFOLD', 'Scaffold', 'nodeScaffoldExecutor', FALSE, 'workflow-io', '["rootDir","yaml","artifactId","groupId"]'),
        ('NODE_DEFAULT', 2, 'ENUM_GENERATION', 'Enum Generation', 'nodeEnumExecutor', FALSE, 'workflow-default', '["rootDir","yaml","artifactId","groupId"]'),
        ('NODE_DEFAULT', 3, 'DTO_GENERATION', 'DTO Generation', 'nodeDtoExecutor', FALSE, 'workflow-default', '["rootDir","yaml","artifactId","groupId"]'),
        ('NODE_DEFAULT', 4, 'MODEL_GENERATION', 'Model Generation', 'nodeModelExecutor', FALSE, 'workflow-default', '["rootDir","yaml","artifactId","groupId"]'),
        ('NODE_DEFAULT', 5, 'REST_GENERATION', 'REST Generation', 'nodeRestExecutor', FALSE, 'workflow-default', '["rootDir","yaml","artifactId","groupId"]'),
        ('NODE_DEFAULT', 6, 'APPLICATION_FILES', 'Application Files', 'nodeApplicationFilesExecutor', FALSE, 'workflow-default', '["rootDir","yaml","artifactId","groupId"]'),
        ('NODE_DEFAULT', 7, 'DOCKER_GENERATION', 'Docker Generation', 'nodeDockerExecutor', TRUE, 'workflow-default', '["rootDir","yaml","artifactId","groupId"]'),
        ('PYTHON_DEFAULT', 1, 'PYTHON_GENERATION', 'Python Generation', 'pythonGenerationExecutor', TRUE, 'workflow-default', '["rootDir","yaml"]')
) AS seed(workflow_code, step_order, step_code, step_name, executor_key, terminal, pool_code, required_inputs_json)
    ON wd.code = seed.workflow_code
WHERE NOT EXISTS (
    SELECT 1
    FROM workflow_steps ws
    WHERE ws.workflow_id = wd.id
      AND ws.step_code = seed.step_code
);

INSERT INTO workflow_transitions (id, workflow_step_id, transition_type, target_step_code, condition_json, priority)
SELECT gen_random_uuid(), ws.id, 'SUCCESS', seed.target_step_code, NULL, 1
FROM workflow_steps ws
JOIN workflow_definitions wd ON wd.id = ws.workflow_id
JOIN (
    VALUES
        ('JAVA_DEFAULT', 'DTO_GENERATION', 'ENUM_GENERATION'),
        ('JAVA_DEFAULT', 'ENUM_GENERATION', 'MODEL_GENERATION'),
        ('JAVA_DEFAULT', 'MODEL_GENERATION', 'SWAGGER_GENERATION'),
        ('JAVA_DEFAULT', 'SWAGGER_GENERATION', 'REST_GENERATION'),
        ('JAVA_DEFAULT', 'REST_GENERATION', 'CRUD_GENERATION'),
        ('JAVA_DEFAULT', 'CRUD_GENERATION', 'APPLICATION_FILES'),
        ('JAVA_DEFAULT', 'APPLICATION_FILES', 'ACTUATOR_CONFIGURATION'),
        ('JAVA_DEFAULT', 'ACTUATOR_CONFIGURATION', 'EXCEPTION_PACKAGE_GENERATION'),
        ('JAVA_DEFAULT', 'EXCEPTION_PACKAGE_GENERATION', 'MAPPER_GENERATION'),
        ('JAVA_DEFAULT', 'MAPPER_GENERATION', 'DOCKER_GENERATION'),
        ('JAVA_DEFAULT', 'DOCKER_GENERATION', 'SCAFFOLD'),
        ('KOTLIN_DEFAULT', 'DTO_GENERATION', 'ENUM_GENERATION'),
        ('KOTLIN_DEFAULT', 'ENUM_GENERATION', 'MODEL_GENERATION'),
        ('KOTLIN_DEFAULT', 'MODEL_GENERATION', 'SWAGGER_GENERATION'),
        ('KOTLIN_DEFAULT', 'SWAGGER_GENERATION', 'REST_GENERATION'),
        ('KOTLIN_DEFAULT', 'REST_GENERATION', 'CRUD_GENERATION'),
        ('KOTLIN_DEFAULT', 'CRUD_GENERATION', 'APPLICATION_FILES'),
        ('KOTLIN_DEFAULT', 'APPLICATION_FILES', 'ACTUATOR_CONFIGURATION'),
        ('KOTLIN_DEFAULT', 'ACTUATOR_CONFIGURATION', 'EXCEPTION_PACKAGE_GENERATION'),
        ('KOTLIN_DEFAULT', 'EXCEPTION_PACKAGE_GENERATION', 'MAPPER_GENERATION'),
        ('KOTLIN_DEFAULT', 'MAPPER_GENERATION', 'DOCKER_GENERATION'),
        ('KOTLIN_DEFAULT', 'DOCKER_GENERATION', 'SCAFFOLD'),
        ('NODE_DEFAULT', 'SCAFFOLD', 'ENUM_GENERATION'),
        ('NODE_DEFAULT', 'ENUM_GENERATION', 'DTO_GENERATION'),
        ('NODE_DEFAULT', 'DTO_GENERATION', 'MODEL_GENERATION'),
        ('NODE_DEFAULT', 'MODEL_GENERATION', 'REST_GENERATION'),
        ('NODE_DEFAULT', 'REST_GENERATION', 'APPLICATION_FILES'),
        ('NODE_DEFAULT', 'APPLICATION_FILES', 'DOCKER_GENERATION')
) AS seed(workflow_code, source_step_code, target_step_code)
    ON wd.code = seed.workflow_code
   AND ws.step_code = seed.source_step_code
WHERE NOT EXISTS (
    SELECT 1
    FROM workflow_transitions wt
    WHERE wt.workflow_step_id = ws.id
      AND wt.transition_type = 'SUCCESS'
      AND wt.target_step_code = seed.target_step_code
);
