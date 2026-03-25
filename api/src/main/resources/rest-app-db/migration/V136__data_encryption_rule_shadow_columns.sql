CREATE TABLE IF NOT EXISTS data_encryption_rule_shadow_columns (
    id uuid PRIMARY KEY,
    rule_id uuid NOT NULL REFERENCES data_encryption_rules(id) ON DELETE CASCADE,
    shadow_column varchar(150) NOT NULL,
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT uq_data_encryption_rule_shadow_columns UNIQUE (rule_id, shadow_column)
);

CREATE INDEX IF NOT EXISTS idx_data_encryption_rule_shadow_columns_rule_id
    ON data_encryption_rule_shadow_columns (rule_id);

INSERT INTO data_encryption_rule_shadow_columns (id, rule_id, shadow_column, created_at, updated_at)
SELECT
    (
        substr(md5(rule.id::text || ':' || lower(trim(rule.hash_shadow_column))), 1, 8) || '-' ||
        substr(md5(rule.id::text || ':' || lower(trim(rule.hash_shadow_column))), 9, 4) || '-' ||
        substr(md5(rule.id::text || ':' || lower(trim(rule.hash_shadow_column))), 13, 4) || '-' ||
        substr(md5(rule.id::text || ':' || lower(trim(rule.hash_shadow_column))), 17, 4) || '-' ||
        substr(md5(rule.id::text || ':' || lower(trim(rule.hash_shadow_column))), 21, 12)
    )::uuid,
    rule.id,
    lower(trim(rule.hash_shadow_column)),
    now(),
    now()
FROM data_encryption_rules rule
WHERE rule.hash_shadow_column IS NOT NULL
  AND trim(rule.hash_shadow_column) <> ''
ON CONFLICT (rule_id, shadow_column) DO NOTHING;
