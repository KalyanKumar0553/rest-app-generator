UPDATE workflow_steps
SET terminal = FALSE,
    updated_at = now()
WHERE workflow_id IN (
    SELECT id
    FROM workflow_definitions
    WHERE code = 'PYTHON_DEFAULT'
      AND version = 1
)
  AND step_code = 'PYTHON_GENERATION';

INSERT INTO workflow_steps (
    id, workflow_id, step_code, step_name, executor_key, enabled, terminal, step_order, pool_code, async_execution,
    timeout_ms, run_condition_json, required_inputs_json, optional_inputs_json, declared_outputs_json,
    retry_enabled, retry_max_attempts, retry_backoff_ms, retry_backoff_multiplier, created_at, updated_at
)
SELECT (
           substr(md5('workflow_step:PYTHON_DEFAULT:MODEL_GENERATION'), 1, 8) || '-' ||
           substr(md5('workflow_step:PYTHON_DEFAULT:MODEL_GENERATION'), 9, 4) || '-' ||
           substr(md5('workflow_step:PYTHON_DEFAULT:MODEL_GENERATION'), 13, 4) || '-' ||
           substr(md5('workflow_step:PYTHON_DEFAULT:MODEL_GENERATION'), 17, 4) || '-' ||
           substr(md5('workflow_step:PYTHON_DEFAULT:MODEL_GENERATION'), 21, 12)
       )::uuid,
       wd.id, 'MODEL_GENERATION', 'Model Generation', 'pythonModelExecutor', TRUE, TRUE, 2,
       'workflow-default', FALSE, 300000, NULL, '["rootDir","yaml"]', '[]', '["status"]',
       TRUE, 2, 500, 2.0, now(), now()
FROM workflow_definitions wd
WHERE wd.code = 'PYTHON_DEFAULT'
  AND wd.version = 1
  AND NOT EXISTS (
      SELECT 1
      FROM workflow_steps ws
      WHERE ws.workflow_id = wd.id
        AND ws.step_code = 'MODEL_GENERATION'
  );

INSERT INTO workflow_transitions (id, workflow_step_id, transition_type, target_step_code, condition_json, priority)
SELECT (
           substr(md5('workflow_transition:PYTHON_DEFAULT:PYTHON_GENERATION:SUCCESS:MODEL_GENERATION'), 1, 8) || '-' ||
           substr(md5('workflow_transition:PYTHON_DEFAULT:PYTHON_GENERATION:SUCCESS:MODEL_GENERATION'), 9, 4) || '-' ||
           substr(md5('workflow_transition:PYTHON_DEFAULT:PYTHON_GENERATION:SUCCESS:MODEL_GENERATION'), 13, 4) || '-' ||
           substr(md5('workflow_transition:PYTHON_DEFAULT:PYTHON_GENERATION:SUCCESS:MODEL_GENERATION'), 17, 4) || '-' ||
           substr(md5('workflow_transition:PYTHON_DEFAULT:PYTHON_GENERATION:SUCCESS:MODEL_GENERATION'), 21, 12)
       )::uuid,
       ws.id, 'SUCCESS', 'MODEL_GENERATION', NULL, 1
FROM workflow_steps ws
JOIN workflow_definitions wd ON wd.id = ws.workflow_id
WHERE wd.code = 'PYTHON_DEFAULT'
  AND wd.version = 1
  AND ws.step_code = 'PYTHON_GENERATION'
  AND NOT EXISTS (
      SELECT 1
      FROM workflow_transitions wt
      WHERE wt.workflow_step_id = ws.id
        AND wt.transition_type = 'SUCCESS'
        AND wt.target_step_code = 'MODEL_GENERATION'
  );
