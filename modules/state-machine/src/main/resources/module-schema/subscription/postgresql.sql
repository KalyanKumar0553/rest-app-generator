CREATE TABLE IF NOT EXISTS subscription_plan (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(100) NOT NULL UNIQUE,
    name VARCHAR(150) NOT NULL,
    description VARCHAR(1000),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    sort_order INTEGER DEFAULT 0,
    trial_days INTEGER,
    plan_type VARCHAR(50) NOT NULL,
    visibility VARCHAR(50) NOT NULL,
    max_users INTEGER,
    max_projects INTEGER,
    max_storage_mb INTEGER,
    metadata_json TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_at TIMESTAMP,
    updated_by VARCHAR(100),
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    entity_version BIGINT
);

CREATE TABLE IF NOT EXISTS subscription_feature (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(100) NOT NULL UNIQUE,
    name VARCHAR(150) NOT NULL,
    description VARCHAR(1000),
    feature_type VARCHAR(50) NOT NULL,
    value_data_type VARCHAR(50),
    unit VARCHAR(50),
    reset_policy VARCHAR(50),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    is_system BOOLEAN NOT NULL DEFAULT FALSE,
    metadata_json TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_at TIMESTAMP,
    updated_by VARCHAR(100),
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    entity_version BIGINT
);

CREATE TABLE IF NOT EXISTS plan_feature_mapping (
    id BIGSERIAL PRIMARY KEY,
    plan_id BIGINT NOT NULL REFERENCES subscription_plan(id),
    feature_id BIGINT NOT NULL REFERENCES subscription_feature(id),
    is_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    limit_value BIGINT,
    decimal_value DECIMAL(19,4),
    string_value VARCHAR(500),
    metadata_json TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_at TIMESTAMP,
    updated_by VARCHAR(100),
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    entity_version BIGINT,
    CONSTRAINT uq_plan_feature_mapping UNIQUE (plan_id, feature_id)
);

CREATE TABLE IF NOT EXISTS plan_price (
    id BIGSERIAL PRIMARY KEY,
    plan_id BIGINT NOT NULL REFERENCES subscription_plan(id),
    billing_cycle VARCHAR(50) NOT NULL,
    currency_code VARCHAR(10) NOT NULL,
    amount DECIMAL(19,2) NOT NULL,
    discount_percent DECIMAL(5,2),
    effective_from TIMESTAMP NOT NULL,
    effective_to TIMESTAMP,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    display_label VARCHAR(100),
    metadata_json TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_at TIMESTAMP,
    updated_by VARCHAR(100),
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    entity_version BIGINT
);

CREATE TABLE IF NOT EXISTS subscription_coupon (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(100) NOT NULL UNIQUE,
    name VARCHAR(150) NOT NULL,
    description VARCHAR(1000),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    discount_type VARCHAR(50) NOT NULL,
    discount_value DECIMAL(19,4) NOT NULL,
    currency_code VARCHAR(10),
    valid_from TIMESTAMP NOT NULL,
    valid_to TIMESTAMP,
    max_redemptions INTEGER,
    max_redemptions_per_tenant INTEGER,
    first_subscription_only BOOLEAN NOT NULL DEFAULT FALSE,
    metadata_json TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_at TIMESTAMP,
    updated_by VARCHAR(100),
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    entity_version BIGINT
);

CREATE TABLE IF NOT EXISTS customer_subscription (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    subscriber_user_id TEXT,
    plan_id BIGINT NOT NULL REFERENCES subscription_plan(id),
    billing_cycle VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    start_at TIMESTAMP NOT NULL,
    end_at TIMESTAMP,
    trial_start_at TIMESTAMP,
    trial_end_at TIMESTAMP,
    auto_renew BOOLEAN NOT NULL DEFAULT FALSE,
    price_snapshot DECIMAL(19,2),
    currency_code VARCHAR(10),
    applied_coupon_id BIGINT REFERENCES subscription_coupon(id) ON DELETE SET NULL,
    applied_coupon_code VARCHAR(100),
    applied_discount_type VARCHAR(50),
    applied_discount_value DECIMAL(19,4),
    applied_discount_amount DECIMAL(19,2),
    plan_code_snapshot VARCHAR(100) NOT NULL,
    source VARCHAR(50) NOT NULL,
    external_reference VARCHAR(255),
    cancelled_at TIMESTAMP,
    cancel_reason VARCHAR(500),
    renewal_attempt_count INTEGER DEFAULT 0,
    scheduled_change_at TIMESTAMP,
    scheduled_target_plan_id BIGINT REFERENCES subscription_plan(id),
    scheduled_target_billing_cycle VARCHAR(50),
    scheduled_target_currency_code VARCHAR(10),
    metadata_json TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_at TIMESTAMP,
    updated_by VARCHAR(100),
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    entity_version BIGINT
);

CREATE TABLE IF NOT EXISTS customer_feature_override (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    feature_id BIGINT NOT NULL REFERENCES subscription_feature(id),
    is_enabled BOOLEAN,
    limit_value BIGINT,
    decimal_value DECIMAL(19,4),
    string_value VARCHAR(500),
    override_type VARCHAR(50) NOT NULL,
    reason VARCHAR(500),
    effective_from TIMESTAMP NOT NULL,
    effective_to TIMESTAMP,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    metadata_json TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_at TIMESTAMP,
    updated_by VARCHAR(100),
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    entity_version BIGINT
);

CREATE TABLE IF NOT EXISTS feature_usage (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    feature_id BIGINT NOT NULL REFERENCES subscription_feature(id),
    period_key VARCHAR(50) NOT NULL,
    period_start TIMESTAMP NOT NULL,
    period_end TIMESTAMP NOT NULL,
    used_value BIGINT NOT NULL DEFAULT 0,
    reserved_value BIGINT NOT NULL DEFAULT 0,
    last_consumed_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_feature_usage UNIQUE (tenant_id, feature_id, period_key)
);

CREATE TABLE IF NOT EXISTS subscription_coupon_plan_mapping (
    id BIGSERIAL PRIMARY KEY,
    coupon_id BIGINT NOT NULL REFERENCES subscription_coupon(id) ON DELETE CASCADE,
    plan_id BIGINT NOT NULL REFERENCES subscription_plan(id) ON DELETE CASCADE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_at TIMESTAMP,
    updated_by VARCHAR(100),
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    entity_version BIGINT,
    CONSTRAINT uq_subscription_coupon_plan UNIQUE (coupon_id, plan_id)
);

CREATE TABLE IF NOT EXISTS subscription_plan_role_mapping (
    id BIGSERIAL PRIMARY KEY,
    plan_id BIGINT NOT NULL REFERENCES subscription_plan(id) ON DELETE CASCADE,
    role_name TEXT NOT NULL REFERENCES roles(name) ON DELETE RESTRICT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_at TIMESTAMP,
    updated_by VARCHAR(100),
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    entity_version BIGINT,
    CONSTRAINT uq_subscription_plan_role UNIQUE (plan_id, role_name)
);

CREATE TABLE IF NOT EXISTS subscription_role_assignment (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    user_id TEXT NOT NULL,
    subscription_id BIGINT NOT NULL REFERENCES customer_subscription(id) ON DELETE CASCADE,
    role_name TEXT NOT NULL REFERENCES roles(name) ON DELETE RESTRICT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    assigned_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    revoked_at TIMESTAMP,
    metadata_json TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_at TIMESTAMP,
    updated_by VARCHAR(100),
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    entity_version BIGINT,
    CONSTRAINT uq_subscription_role_assignment UNIQUE (tenant_id, user_id, subscription_id, role_name)
);

CREATE TABLE IF NOT EXISTS subscription_coupon_redemption (
    id BIGSERIAL PRIMARY KEY,
    coupon_id BIGINT NOT NULL REFERENCES subscription_coupon(id) ON DELETE RESTRICT,
    subscription_id BIGINT NOT NULL REFERENCES customer_subscription(id) ON DELETE CASCADE,
    tenant_id BIGINT NOT NULL,
    user_id TEXT,
    coupon_code_snapshot VARCHAR(100) NOT NULL,
    discount_type_snapshot VARCHAR(50) NOT NULL,
    discount_value_snapshot DECIMAL(19,4) NOT NULL,
    discount_amount_snapshot DECIMAL(19,2) NOT NULL,
    currency_code VARCHAR(10),
    redeemed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    metadata_json TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_at TIMESTAMP,
    updated_by VARCHAR(100),
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    entity_version BIGINT,
    CONSTRAINT uq_subscription_coupon_redemption UNIQUE (coupon_id, subscription_id)
);

CREATE TABLE IF NOT EXISTS subscription_audit_log (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    subscription_id BIGINT REFERENCES customer_subscription(id),
    event_type VARCHAR(100) NOT NULL,
    old_plan_id BIGINT REFERENCES subscription_plan(id),
    new_plan_id BIGINT REFERENCES subscription_plan(id),
    old_status VARCHAR(50),
    new_status VARCHAR(50),
    actor_type VARCHAR(50) NOT NULL,
    actor_id VARCHAR(100),
    reason VARCHAR(500),
    payload_json TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_subscription_plan_code ON subscription_plan (code);
CREATE INDEX IF NOT EXISTS idx_subscription_feature_code ON subscription_feature (code);
CREATE INDEX IF NOT EXISTS idx_plan_feature_mapping_plan_feature ON plan_feature_mapping (plan_id, feature_id);
CREATE INDEX IF NOT EXISTS idx_plan_price_lookup ON plan_price (plan_id, billing_cycle, currency_code, effective_from);
CREATE INDEX IF NOT EXISTS idx_customer_subscription_tenant_status ON customer_subscription (tenant_id, status, deleted);
CREATE INDEX IF NOT EXISTS idx_customer_override_lookup ON customer_feature_override (tenant_id, feature_id, is_active, effective_from, effective_to);
CREATE INDEX IF NOT EXISTS idx_feature_usage_lookup ON feature_usage (tenant_id, feature_id, period_key);
CREATE INDEX IF NOT EXISTS idx_subscription_coupon_code ON subscription_coupon (code);
CREATE INDEX IF NOT EXISTS idx_subscription_coupon_validity ON subscription_coupon (is_active, valid_from, valid_to, deleted);
CREATE INDEX IF NOT EXISTS idx_subscription_coupon_plan_map ON subscription_coupon_plan_mapping (coupon_id, plan_id);
CREATE INDEX IF NOT EXISTS idx_subscription_plan_role_map ON subscription_plan_role_mapping (plan_id, role_name);
CREATE INDEX IF NOT EXISTS idx_subscription_role_assignment_user ON subscription_role_assignment (user_id, is_active, deleted);
CREATE INDEX IF NOT EXISTS idx_subscription_role_assignment_tenant ON subscription_role_assignment (tenant_id, user_id, is_active, deleted);
CREATE INDEX IF NOT EXISTS idx_subscription_coupon_redemption_coupon ON subscription_coupon_redemption (coupon_id, tenant_id, deleted);
CREATE INDEX IF NOT EXISTS idx_subscription_coupon_redemption_user ON subscription_coupon_redemption (user_id, coupon_id, deleted);

INSERT INTO permissions (name, display_name, description, category, active)
VALUES
    ('subscription.plan.read', 'View Subscription Plans', 'View subscription plan catalog', 'SUBSCRIPTION', TRUE),
    ('subscription.plan.manage', 'Manage Subscription Plans', 'Create and update subscription plans', 'SUBSCRIPTION', TRUE),
    ('subscription.feature.read', 'View Subscription Features', 'View feature catalog and plan entitlements', 'SUBSCRIPTION', TRUE),
    ('subscription.feature.manage', 'Manage Subscription Features', 'Manage features and plan feature mappings', 'SUBSCRIPTION', TRUE),
    ('subscription.price.read', 'View Subscription Pricing', 'View subscription plan pricing', 'SUBSCRIPTION', TRUE),
    ('subscription.price.manage', 'Manage Subscription Pricing', 'Create and update subscription prices', 'SUBSCRIPTION', TRUE),
    ('subscription.manage', 'Manage Tenant Subscriptions', 'Assign, upgrade, downgrade, renew and cancel subscriptions', 'SUBSCRIPTION', TRUE),
    ('subscription.override.manage', 'Manage Subscription Overrides', 'Manage tenant-level feature overrides', 'SUBSCRIPTION', TRUE),
    ('subscription.audit.read', 'View Subscription Audit', 'View tenant subscription audit history', 'SUBSCRIPTION', TRUE),
    ('subscription.self.read', 'View Own Subscription', 'View current tenant subscription and entitlements', 'SUBSCRIPTION', TRUE),
    ('subscription.self.upgrade', 'Upgrade Own Subscription', 'Upgrade current tenant subscription', 'SUBSCRIPTION', TRUE),
    ('subscription.self.cancel', 'Cancel Own Subscription', 'Cancel current tenant subscription', 'SUBSCRIPTION', TRUE),
    ('subscription.coupon.read', 'View Subscription Coupons', 'View subscription coupons and discount rules', 'SUBSCRIPTION', TRUE),
    ('subscription.coupon.manage', 'Manage Subscription Coupons', 'Create, update, and activate subscription coupons', 'SUBSCRIPTION', TRUE)
ON CONFLICT (name) DO UPDATE
SET display_name = EXCLUDED.display_name,
    description = EXCLUDED.description,
    category = EXCLUDED.category,
    active = EXCLUDED.active,
    updated_at = now();

INSERT INTO role_permissions (role_name, permission_name)
VALUES
    ('ROLE_SUPER_ADMIN', 'subscription.plan.read'),
    ('ROLE_SUPER_ADMIN', 'subscription.plan.manage'),
    ('ROLE_SUPER_ADMIN', 'subscription.feature.read'),
    ('ROLE_SUPER_ADMIN', 'subscription.feature.manage'),
    ('ROLE_SUPER_ADMIN', 'subscription.price.read'),
    ('ROLE_SUPER_ADMIN', 'subscription.price.manage'),
    ('ROLE_SUPER_ADMIN', 'subscription.manage'),
    ('ROLE_SUPER_ADMIN', 'subscription.override.manage'),
    ('ROLE_SUPER_ADMIN', 'subscription.audit.read'),
    ('ROLE_SUPER_ADMIN', 'subscription.self.read'),
    ('ROLE_SUPER_ADMIN', 'subscription.self.upgrade'),
    ('ROLE_SUPER_ADMIN', 'subscription.self.cancel'),
    ('ROLE_SUPER_ADMIN', 'subscription.coupon.read'),
    ('ROLE_SUPER_ADMIN', 'subscription.coupon.manage')
ON CONFLICT DO NOTHING;

INSERT INTO subscription_plan (
    code, name, description, is_active, is_default, sort_order, trial_days, plan_type, visibility,
    max_users, max_projects, max_storage_mb, created_at, deleted, entity_version
)
VALUES
    ('FREE', 'Free', 'Free tier for evaluation and starter usage', TRUE, TRUE, 1, 0, 'FREE', 'PUBLIC', 1, 2, 512, CURRENT_TIMESTAMP, FALSE, 0),
    ('PRO', 'Pro', 'Professional plan for growing teams', TRUE, FALSE, 2, 14, 'PAID', 'PUBLIC', 25, 100, 5120, CURRENT_TIMESTAMP, FALSE, 0)
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
    deleted = FALSE;

UPDATE subscription_plan
SET is_default = CASE WHEN code = 'FREE' THEN TRUE ELSE FALSE END
WHERE code IN ('FREE', 'PRO');

INSERT INTO subscription_feature (
    code, name, description, feature_type, value_data_type, unit, reset_policy, is_active, is_system, created_at, deleted, entity_version
)
VALUES
    ('CUSTOM_BRANDING', 'Custom Branding', 'Enable custom branding support', 'BOOLEAN', 'BOOLEAN', NULL, 'NEVER', TRUE, FALSE, CURRENT_TIMESTAMP, FALSE, 0),
    ('ADVANCED_ANALYTICS', 'Advanced Analytics', 'Access premium analytics and reports', 'BOOLEAN', 'BOOLEAN', NULL, 'NEVER', TRUE, FALSE, CURRENT_TIMESTAMP, FALSE, 0),
    ('API_ACCESS', 'API Access', 'Allow API access to generated resources', 'BOOLEAN', 'BOOLEAN', NULL, 'NEVER', TRUE, FALSE, CURRENT_TIMESTAMP, FALSE, 0),
    ('MAX_PROJECTS', 'Max Projects', 'Maximum number of managed projects', 'LIMIT', 'INTEGER', 'PROJECTS', 'NEVER', TRUE, FALSE, CURRENT_TIMESTAMP, FALSE, 0),
    ('MAX_TEAM_MEMBERS', 'Max Team Members', 'Maximum number of project collaborators', 'LIMIT', 'INTEGER', 'USERS', 'NEVER', TRUE, FALSE, CURRENT_TIMESTAMP, FALSE, 0),
    ('MAX_API_REQUESTS_PER_MONTH', 'Max API Requests Per Month', 'Monthly API request quota', 'QUOTA', 'INTEGER', 'REQUESTS_PER_MONTH', 'MONTHLY', TRUE, FALSE, CURRENT_TIMESTAMP, FALSE, 0)
ON CONFLICT (code) DO UPDATE
SET name = EXCLUDED.name,
    description = EXCLUDED.description,
    feature_type = EXCLUDED.feature_type,
    value_data_type = EXCLUDED.value_data_type,
    unit = EXCLUDED.unit,
    reset_policy = EXCLUDED.reset_policy,
    is_active = EXCLUDED.is_active,
    is_system = EXCLUDED.is_system,
    deleted = FALSE;

INSERT INTO plan_feature_mapping (
    plan_id, feature_id, is_enabled, limit_value, created_at, deleted, entity_version
)
SELECT p.id, f.id,
       CASE
           WHEN f.code IN ('CUSTOM_BRANDING', 'ADVANCED_ANALYTICS', 'API_ACCESS') THEN FALSE
           ELSE TRUE
       END,
       CASE f.code
           WHEN 'MAX_PROJECTS' THEN 2
           WHEN 'MAX_TEAM_MEMBERS' THEN 1
           WHEN 'MAX_API_REQUESTS_PER_MONTH' THEN 1000
           ELSE NULL
       END,
       CURRENT_TIMESTAMP,
       FALSE,
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
    deleted = FALSE;

INSERT INTO plan_feature_mapping (
    plan_id, feature_id, is_enabled, limit_value, created_at, deleted, entity_version
)
SELECT p.id, f.id,
       TRUE,
       CASE f.code
           WHEN 'MAX_PROJECTS' THEN 100
           WHEN 'MAX_TEAM_MEMBERS' THEN 25
           WHEN 'MAX_API_REQUESTS_PER_MONTH' THEN 100000
           ELSE NULL
       END,
       CURRENT_TIMESTAMP,
       FALSE,
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
    deleted = FALSE;

INSERT INTO plan_price (
    plan_id, billing_cycle, currency_code, amount, discount_percent, effective_from, is_active, display_label, created_at, deleted, entity_version
)
SELECT p.id, x.billing_cycle, 'INR', x.amount, 0, CURRENT_TIMESTAMP, TRUE, x.display_label, CURRENT_TIMESTAMP, FALSE, 0
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
        AND pp.deleted = FALSE
  );
