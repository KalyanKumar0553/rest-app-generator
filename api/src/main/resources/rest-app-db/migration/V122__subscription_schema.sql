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

CREATE TABLE IF NOT EXISTS customer_subscription (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
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

INSERT INTO permissions (name, display_name, description, category, active)
VALUES
    ('subscription.plan.read', 'View Subscription Plans', 'View subscription plan catalog', 'SUBSCRIPTION', true),
    ('subscription.plan.manage', 'Manage Subscription Plans', 'Create and update subscription plans', 'SUBSCRIPTION', true),
    ('subscription.feature.read', 'View Subscription Features', 'View feature catalog and plan entitlements', 'SUBSCRIPTION', true),
    ('subscription.feature.manage', 'Manage Subscription Features', 'Manage features and plan feature mappings', 'SUBSCRIPTION', true),
    ('subscription.price.read', 'View Subscription Pricing', 'View subscription plan pricing', 'SUBSCRIPTION', true),
    ('subscription.price.manage', 'Manage Subscription Pricing', 'Create and update subscription prices', 'SUBSCRIPTION', true),
    ('subscription.manage', 'Manage Tenant Subscriptions', 'Assign, upgrade, downgrade, renew and cancel subscriptions', 'SUBSCRIPTION', true),
    ('subscription.override.manage', 'Manage Subscription Overrides', 'Manage tenant-level feature overrides', 'SUBSCRIPTION', true),
    ('subscription.audit.read', 'View Subscription Audit', 'View tenant subscription audit history', 'SUBSCRIPTION', true),
    ('subscription.self.read', 'View Own Subscription', 'View current tenant subscription and entitlements', 'SUBSCRIPTION', true),
    ('subscription.self.upgrade', 'Upgrade Own Subscription', 'Upgrade current tenant subscription', 'SUBSCRIPTION', true),
    ('subscription.self.cancel', 'Cancel Own Subscription', 'Cancel current tenant subscription', 'SUBSCRIPTION', true)
ON CONFLICT (name) DO UPDATE
SET display_name = EXCLUDED.display_name,
    description = EXCLUDED.description,
    category = EXCLUDED.category,
    active = EXCLUDED.active;

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
    ('ROLE_SUPER_ADMIN', 'subscription.self.cancel')
ON CONFLICT DO NOTHING;
