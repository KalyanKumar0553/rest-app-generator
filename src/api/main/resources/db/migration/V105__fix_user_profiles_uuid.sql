ALTER TABLE IF EXISTS user_profiles
    DROP CONSTRAINT IF EXISTS user_profiles_pkey;

ALTER TABLE IF EXISTS user_profiles
    DROP CONSTRAINT IF EXISTS user_profiles_user_id_fkey;

ALTER TABLE IF EXISTS user_profiles
    ALTER COLUMN user_id TYPE uuid USING user_id::uuid;

ALTER TABLE IF EXISTS user_profiles
    ADD PRIMARY KEY (user_id);

ALTER TABLE IF EXISTS user_profiles
    ADD CONSTRAINT user_profiles_user_id_fkey
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;
