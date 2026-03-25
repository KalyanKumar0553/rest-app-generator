ALTER TABLE config_property
    ADD COLUMN IF NOT EXISTS current_value_key VARCHAR(200);

INSERT INTO config_property (category, label, property_key, current_value_key)
VALUES ('FEATURES', 'AI Labs', 'app.feature.ai-labs.enabled', 'false')
ON CONFLICT (property_key) DO UPDATE
SET category = EXCLUDED.category,
    label = EXCLUDED.label;

DELETE FROM config_property_values
WHERE property_id = (SELECT id FROM config_property WHERE property_key = 'app.feature.ai-labs.enabled');

INSERT INTO config_property_values (property_id, value_key, value_label)
VALUES
((SELECT id FROM config_property WHERE property_key = 'app.feature.ai-labs.enabled'), 'true', 'Enabled'),
((SELECT id FROM config_property WHERE property_key = 'app.feature.ai-labs.enabled'), 'false', 'Disabled');
