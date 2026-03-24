import type { Express } from 'express';

import { resolveAuthConfig } from './config/auth-config';
import { AdminAuthController } from './controller/admin-auth.controller';
import { AuthController } from './controller/auth.controller';
import { HealthController } from './controller/health.controller';
import { ProfileController } from './controller/profile.controller';
import { buildAdminAuthRouter } from './route/admin-auth.routes';
import { buildAuthRouter } from './route/auth.routes';
import { buildHealthRouter } from './route/health.routes';
import { RefreshTokenRepository } from './repository/refresh-token.repository';
import { UserRepository } from './repository/user.repository';
import { jwtAuthenticationMiddleware } from './security/jwt-authentication.middleware';
import { AuthService } from './service/auth.service';
import { CaptchaService } from './service/captcha.service';
import { OAuthProviderService } from './service/oauth-provider.service';

type ModuleManifest = {
  generator: string;
  selectedModules: string[];
  moduleConfigs?: Record<string, Record<string, unknown>>;
};

export const registerModule = (
  app: Express,
  config: Record<string, unknown>,
  _manifest: ModuleManifest
): void => {
  const resolvedConfig = resolveAuthConfig(config);
  const userRepository = new UserRepository();
  const refreshTokenRepository = new RefreshTokenRepository();
  const authService = new AuthService(resolvedConfig, userRepository, refreshTokenRepository);
  const providerService = new OAuthProviderService(resolvedConfig);
  const captchaService = new CaptchaService();
  const authController = new AuthController(authService, providerService, resolvedConfig);
  const profileController = new ProfileController(authService);
  const adminAuthController = new AdminAuthController(userRepository, providerService);
  const healthController = new HealthController();
  app.use('/actuator', buildHealthRouter(healthController));
  app.use('/api/auth', buildAuthRouter(authController, profileController));
  app.use('/api/admin/auth', jwtAuthenticationMiddleware(resolvedConfig.jwtSecret), buildAdminAuthRouter(adminAuthController));
  app.get('/api/auth/captcha', (_request, response) => {
    response.json(captchaService.createChallenge());
  });
};
