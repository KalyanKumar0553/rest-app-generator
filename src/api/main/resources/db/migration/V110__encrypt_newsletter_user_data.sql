ALTER TABLE newsletter_subscriptions
    ALTER COLUMN email TYPE VARCHAR(1024);

ALTER TABLE newsletter_subscriptions
    ADD COLUMN IF NOT EXISTS email_hash VARCHAR(64);

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
          FROM information_schema.table_constraints
         WHERE table_name = 'newsletter_subscriptions'
           AND constraint_name = 'newsletter_subscriptions_email_key'
           AND constraint_type = 'UNIQUE'
    ) THEN
        ALTER TABLE newsletter_subscriptions
            DROP CONSTRAINT newsletter_subscriptions_email_key;
    END IF;
END $$;

CREATE UNIQUE INDEX IF NOT EXISTS uq_newsletter_subscriptions_email_hash
    ON newsletter_subscriptions (email_hash)
    WHERE email_hash IS NOT NULL;

