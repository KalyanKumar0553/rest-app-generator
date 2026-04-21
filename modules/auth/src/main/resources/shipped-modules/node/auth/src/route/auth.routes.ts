import { Router } from 'express';

import { AuthController } from '../controller/auth.controller';
import { ProfileController } from '../controller/profile.controller';

export const buildAuthRouter = (
  authController: AuthController,
  profileController: ProfileController
): Router => {
  const router = Router();
  router.get('/status', authController.status);
  router.get('/providers', authController.providers);
  router.post('/register', authController.register);
  router.post('/login', authController.login);
  router.post('/refresh', authController.refresh);
  router.get('/me', profileController.me);
  return router;
};
