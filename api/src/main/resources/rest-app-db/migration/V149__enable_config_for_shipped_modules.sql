UPDATE plugin_module
SET enable_config = true,
    updated_at = now()
WHERE lower(code) IN ('rbac', 'auth', 'state-machine', 'subscription', 'swagger', 'cdn');
