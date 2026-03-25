INSERT INTO subscription_plan (
    code, name, description, is_active, is_default, sort_order, trial_days, plan_type, visibility,
    max_users, max_projects, max_storage_mb, created_at, deleted, entity_version
)
VALUES
    ('FREE', 'Free', 'Free tier for evaluation and starter usage', true, true, 1, 0, 'FREE', 'PUBLIC', 1, 2, 512, CURRENT_TIMESTAMP, false, 0),
    ('PRO', 'Pro', 'Professional plan for growing teams', true, false, 2, 14, 'PAID', 'PUBLIC', 25, 100, 5120, CURRENT_TIMESTAMP, false, 0)
ON CONFLICT (code) DO UPDATE
SET name = EXCLUDED.name,
    description = EXCLUDED.description,
    is_active = EXCLUDED.is_active,
    sort_order = EXCLUDED.sort_order,
    trial_days = EXCLUDED.trial_days,
    plan_type = EXCLUDED.plan_type,
    visibility = EXCLUDED.visibility,
    max_users = EXCLUDED.max_users,
    max_projects = EXCLUDED.max_projects,
    max_storage_mb = EXCLUDED.max_storage_mb,
    deleted = false;

UPDATE subscription_plan
SET is_default = CASE WHEN code = 'FREE' THEN true ELSE false END
WHERE code IN ('FREE', 'PRO');

INSERT INTO subscription_feature (
    code, name, description, feature_type, value_data_type, unit, reset_policy, is_active, is_system, created_at, deleted, entity_version
)
VALUES
    ('CUSTOM_BRANDING', 'Custom Branding', 'Enable custom branding support', 'BOOLEAN', 'BOOLEAN', NULL, 'NEVER', true, false, CURRENT_TIMESTAMP, false, 0),
    ('ADVANCED_ANALYTICS', 'Advanced Analytics', 'Access premium analytics and reports', 'BOOLEAN', 'BOOLEAN', NULL, 'NEVER', true, false, CURRENT_TIMESTAMP, false, 0),
    ('API_ACCESS', 'API Access', 'Allow API access to generated resources', 'BOOLEAN', 'BOOLEAN', NULL, 'NEVER', true, false, CURRENT_TIMESTAMP, false, 0),
    ('MAX_PROJECTS', 'Max Projects', 'Maximum number of managed projects', 'LIMIT', 'INTEGER', 'PROJECTS', 'NEVER', true, false, CURRENT_TIMESTAMP, false, 0),
    ('MAX_TEAM_MEMBERS', 'Max Team Members', 'Maximum number of project collaborators', 'LIMIT', 'INTEGER', 'USERS', 'NEVER', true, false, CURRENT_TIMESTAMP, false, 0),
    ('MAX_API_REQUESTS_PER_MONTH', 'Max API Requests Per Month', 'Monthly API request quota', 'QUOTA', 'INTEGER', 'REQUESTS_PER_MONTH', 'MONTHLY', true, false, CURRENT_TIMESTAMP, false, 0)
ON CONFLICT (code) DO UPDATE
SET name = EXCLUDED.name,
    description = EXCLUDED.description,
    feature_type = EXCLUDED.feature_type,
    value_data_type = EXCLUDED.value_data_type,
    unit = EXCLUDED.unit,
    reset_policy = EXCLUDED.reset_policy,
    is_active = EXCLUDED.is_active,
    is_system = EXCLUDED.is_system,
    deleted = false;

INSERT INTO plan_feature_mapping (
    plan_id, feature_id, is_enabled, limit_value, created_at, deleted, entity_version
)
SELECT p.id, f.id,
       CASE
           WHEN f.code IN ('CUSTOM_BRANDING', 'ADVANCED_ANALYTICS', 'API_ACCESS') THEN false
           ELSE true
       END,
       CASE f.code
           WHEN 'MAX_PROJECTS' THEN 2
           WHEN 'MAX_TEAM_MEMBERS' THEN 1
           WHEN 'MAX_API_REQUESTS_PER_MONTH' THEN 1000
           ELSE NULL
       END,
       CURRENT_TIMESTAMP,
       false,
       0
FROM subscription_plan p
JOIN subscription_feature f ON f.code IN (
    'CUSTOM_BRANDING', 'ADVANCED_ANALYTICS', 'API_ACCESS',
    'MAX_PROJECTS', 'MAX_TEAM_MEMBERS', 'MAX_API_REQUESTS_PER_MONTH'
)
WHERE p.code = 'FREE'
ON CONFLICT (plan_id, feature_id) DO UPDATE
SET is_enabled = EXCLUDED.is_enabled,
    limit_value = EXCLUDED.limit_value,
    deleted = false;

INSERT INTO plan_feature_mapping (
    plan_id, feature_id, is_enabled, limit_value, created_at, deleted, entity_version
)
SELECT p.id, f.id,
       CASE
           WHEN f.code IN ('CUSTOM_BRANDING', 'ADVANCED_ANALYTICS', 'API_ACCESS') THEN true
           ELSE true
       END,
       CASE f.code
           WHEN 'MAX_PROJECTS' THEN 100
           WHEN 'MAX_TEAM_MEMBERS' THEN 25
           WHEN 'MAX_API_REQUESTS_PER_MONTH' THEN 100000
           ELSE NULL
       END,
       CURRENT_TIMESTAMP,
       false,
       0
FROM subscription_plan p
JOIN subscription_feature f ON f.code IN (
    'CUSTOM_BRANDING', 'ADVANCED_ANALYTICS', 'API_ACCESS',
    'MAX_PROJECTS', 'MAX_TEAM_MEMBERS', 'MAX_API_REQUESTS_PER_MONTH'
)
WHERE p.code = 'PRO'
ON CONFLICT (plan_id, feature_id) DO UPDATE
SET is_enabled = EXCLUDED.is_enabled,
    limit_value = EXCLUDED.limit_value,
    deleted = false;

INSERT INTO plan_price (
    plan_id, billing_cycle, currency_code, amount, discount_percent, effective_from, is_active, display_label, created_at, deleted, entity_version
)
SELECT p.id, x.billing_cycle, 'INR', x.amount, 0, CURRENT_TIMESTAMP, true, x.display_label, CURRENT_TIMESTAMP, false, 0
FROM subscription_plan p
JOIN (
    VALUES
        ('MONTHLY', 499.00::DECIMAL(19,2), '₹499 / month'),
        ('HALF_YEARLY', 2699.00::DECIMAL(19,2), '₹2699 / 6 months'),
        ('YEARLY', 4999.00::DECIMAL(19,2), '₹4999 / year')
) AS x(billing_cycle, amount, display_label) ON TRUE
WHERE p.code = 'PRO'
  AND NOT EXISTS (
      SELECT 1
      FROM plan_price pp
      WHERE pp.plan_id = p.id
        AND pp.billing_cycle = x.billing_cycle
        AND pp.currency_code = 'INR'
        AND pp.deleted = false
  );
