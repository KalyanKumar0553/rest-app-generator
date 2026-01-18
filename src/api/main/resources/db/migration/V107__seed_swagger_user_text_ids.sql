INSERT INTO roles (name) VALUES ('ROLE_USER') ON CONFLICT DO NOTHING;
INSERT INTO roles (name) VALUES ('ROLE_SWAGGER_ADMIN') ON CONFLICT DO NOTHING;

INSERT INTO users (id, identifier, identifier_type, password_hash, status, failed_login_attempts, created_at, updated_at)
VALUES ('swagger-seed', 'swagger', 'EMAIL', '$2a$12$iKz7e976Yz8QE3OsI2HWX.4DVtJaODfA14qV5gsNtVz7gnHFyFAkK', 'ACTIVE', 0, now(), now())
ON CONFLICT (identifier) DO UPDATE
    SET password_hash = EXCLUDED.password_hash,
        status = EXCLUDED.status,
        updated_at = now();

INSERT INTO user_roles (user_id, role_name)
VALUES ('swagger-seed', 'ROLE_USER')
ON CONFLICT DO NOTHING;

INSERT INTO user_roles (user_id, role_name)
VALUES ('swagger-seed', 'ROLE_SWAGGER_ADMIN')
ON CONFLICT DO NOTHING;
