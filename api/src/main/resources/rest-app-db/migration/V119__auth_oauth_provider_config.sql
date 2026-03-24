CREATE TABLE IF NOT EXISTS auth_oauth_provider_config (
    provider_id VARCHAR(50) PRIMARY KEY,
    enabled BOOLEAN NOT NULL DEFAULT FALSE,
    client_id VARCHAR(300),
    client_secret VARCHAR(1000),
    issuer_uri VARCHAR(500),
    scope VARCHAR(300)
);

INSERT INTO auth_oauth_provider_config (provider_id, enabled, client_id, client_secret, issuer_uri, scope)
VALUES ('keycloak', FALSE, NULL, NULL, NULL, 'openid,profile,email')
ON CONFLICT (provider_id) DO NOTHING;
