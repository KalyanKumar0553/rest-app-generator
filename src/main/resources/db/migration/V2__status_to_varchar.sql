-- Convert PostgreSQL enum 'project_status' to VARCHAR(20) for JPA compatibility
DO $$
BEGIN
  -- If column exists, try to alter it to varchar using text cast (works even if already varchar)
  BEGIN
    ALTER TABLE projects ALTER COLUMN status TYPE varchar(20) USING status::text;
  EXCEPTION WHEN undefined_table THEN
    -- projects table may not exist (fresh DB); ignore
    NULL;
  END;

  -- Drop the enum type if it exists
  BEGIN
    DROP TYPE IF EXISTS project_status CASCADE;
  EXCEPTION WHEN undefined_object THEN
    NULL;
  END;
END $$;
