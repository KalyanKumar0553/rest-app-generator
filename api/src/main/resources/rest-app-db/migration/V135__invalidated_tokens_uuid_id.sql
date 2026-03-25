DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name = 'invalidated_tokens'
          AND column_name = 'id'
          AND data_type <> 'uuid'
    ) THEN
        ALTER TABLE invalidated_tokens
            ALTER COLUMN id TYPE uuid
            USING id::uuid;
    END IF;
END $$;
