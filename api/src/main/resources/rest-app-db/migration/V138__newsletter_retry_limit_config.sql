INSERT INTO config_property (category, label, property_key, current_value_key)
VALUES ('FEATURES', 'Newsletter Email Retry Limit', 'app.newsletter.max-email-retry-attempts', '3')
ON CONFLICT (property_key) DO UPDATE
SET category = EXCLUDED.category,
    label = EXCLUDED.label,
    current_value_key = EXCLUDED.current_value_key;

DELETE FROM config_property_values
WHERE property_id = (SELECT id FROM config_property WHERE property_key = 'app.newsletter.max-email-retry-attempts');

INSERT INTO config_property_values (property_id, value_key, value_label)
VALUES
    ((SELECT id FROM config_property WHERE property_key = 'app.newsletter.max-email-retry-attempts'), '1', '1 attempt'),
    ((SELECT id FROM config_property WHERE property_key = 'app.newsletter.max-email-retry-attempts'), '3', '3 attempts'),
    ((SELECT id FROM config_property WHERE property_key = 'app.newsletter.max-email-retry-attempts'), '5', '5 attempts'),
    ((SELECT id FROM config_property WHERE property_key = 'app.newsletter.max-email-retry-attempts'), '10', '10 attempts'),
    ((SELECT id FROM config_property WHERE property_key = 'app.newsletter.max-email-retry-attempts'), '-1', 'Unlimited')
ON CONFLICT DO NOTHING;
