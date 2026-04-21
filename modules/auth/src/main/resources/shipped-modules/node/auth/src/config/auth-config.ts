export type AuthModuleConfig = {
  publicEndpoints?: string[];
  jwtSecret?: string;
  accessTokenExpiresIn?: string;
  refreshTokenExpiresIn?: string;
  oauthProviders?: Array<{
    provider: string;
    enabled: boolean;
    clientId?: string;
  }>;
};

export const resolveAuthConfig = (config: Record<string, unknown>): Required<AuthModuleConfig> => ({
  publicEndpoints: Array.isArray(config.publicEndpoints) && config.publicEndpoints.length
    ? config.publicEndpoints.map((endpoint) => String(endpoint))
    : ['/api/auth/status', '/api/auth/login', '/api/auth/refresh'],
  jwtSecret: typeof config.jwtSecret === 'string' && config.jwtSecret.trim()
    ? config.jwtSecret.trim()
    : 'change-me',
  accessTokenExpiresIn: typeof config.accessTokenExpiresIn === 'string' && config.accessTokenExpiresIn.trim()
    ? config.accessTokenExpiresIn.trim()
    : '1h',
  refreshTokenExpiresIn: typeof config.refreshTokenExpiresIn === 'string' && config.refreshTokenExpiresIn.trim()
    ? config.refreshTokenExpiresIn.trim()
    : '7d',
  oauthProviders: Array.isArray(config.oauthProviders)
    ? config.oauthProviders.map((provider) => ({
        provider: String((provider as Record<string, unknown>).provider ?? 'custom'),
        enabled: Boolean((provider as Record<string, unknown>).enabled),
        clientId: typeof (provider as Record<string, unknown>).clientId === 'string'
          ? String((provider as Record<string, unknown>).clientId)
          : undefined
      }))
    : []
});
