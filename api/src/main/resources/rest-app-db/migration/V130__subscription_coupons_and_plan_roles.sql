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
    user_id TEXT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
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
    user_id TEXT REFERENCES users(id) ON DELETE SET NULL,
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

ALTER TABLE customer_subscription
    ADD COLUMN IF NOT EXISTS subscriber_user_id TEXT REFERENCES users(id) ON DELETE SET NULL,
    ADD COLUMN IF NOT EXISTS applied_coupon_id BIGINT REFERENCES subscription_coupon(id) ON DELETE SET NULL,
    ADD COLUMN IF NOT EXISTS applied_coupon_code VARCHAR(100),
    ADD COLUMN IF NOT EXISTS applied_discount_type VARCHAR(50),
    ADD COLUMN IF NOT EXISTS applied_discount_value DECIMAL(19,4),
    ADD COLUMN IF NOT EXISTS applied_discount_amount DECIMAL(19,2);

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
    ('subscription.coupon.read', 'View Subscription Coupons', 'View subscription coupons and discount rules', 'SUBSCRIPTION', true),
    ('subscription.coupon.manage', 'Manage Subscription Coupons', 'Create, update, and activate subscription coupons', 'SUBSCRIPTION', true)
ON CONFLICT (name) DO UPDATE
SET display_name = EXCLUDED.display_name,
    description = EXCLUDED.description,
    category = EXCLUDED.category,
    active = EXCLUDED.active;

INSERT INTO role_permissions (role_name, permission_name)
VALUES
    ('ROLE_SUPER_ADMIN', 'subscription.coupon.read'),
    ('ROLE_SUPER_ADMIN', 'subscription.coupon.manage')
ON CONFLICT DO NOTHING;
