CREATE TABLE IF NOT EXISTS data_encryption_rules (
    id UUID PRIMARY KEY,
    table_name VARCHAR(150) NOT NULL,
    column_name VARCHAR(150),
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_data_encryption_rules_table_name
    ON data_encryption_rules(table_name);

CREATE UNIQUE INDEX IF NOT EXISTS uq_data_encryption_rules_table_column
    ON data_encryption_rules(table_name, COALESCE(column_name, '__all__'));
