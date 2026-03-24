ALTER TABLE user_profiles
    ADD COLUMN IF NOT EXISTS time_zone_id VARCHAR(100);
