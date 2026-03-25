CREATE TABLE IF NOT EXISTS ai_labs_usage (
    owner_user_id varchar(100) PRIMARY KEY,
    usage_count integer NOT NULL DEFAULT 0,
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now()
);

INSERT INTO config_property (category, label, property_key, current_value_key)
VALUES ('FEATURES', 'AI Labs Usage Limit', 'app.feature.ai-labs.usage-limit', '5')
ON CONFLICT (property_key) DO UPDATE
SET category = EXCLUDED.category,
    label = EXCLUDED.label,
    current_value_key = EXCLUDED.current_value_key;

DELETE FROM config_property_values
WHERE property_id = (SELECT id FROM config_property WHERE property_key = 'app.feature.ai-labs.usage-limit');

INSERT INTO config_property_values (property_id, value_key, value_label)
VALUES
    ((SELECT id FROM config_property WHERE property_key = 'app.feature.ai-labs.usage-limit'), '-1', 'Unlimited'),
    ((SELECT id FROM config_property WHERE property_key = 'app.feature.ai-labs.usage-limit'), '1', '1 use'),
    ((SELECT id FROM config_property WHERE property_key = 'app.feature.ai-labs.usage-limit'), '3', '3 uses'),
    ((SELECT id FROM config_property WHERE property_key = 'app.feature.ai-labs.usage-limit'), '5', '5 uses'),
    ((SELECT id FROM config_property WHERE property_key = 'app.feature.ai-labs.usage-limit'), '10', '10 uses'),
    ((SELECT id FROM config_property WHERE property_key = 'app.feature.ai-labs.usage-limit'), '25', '25 uses'),
    ((SELECT id FROM config_property WHERE property_key = 'app.feature.ai-labs.usage-limit'), '50', '50 uses')
ON CONFLICT DO NOTHING;
