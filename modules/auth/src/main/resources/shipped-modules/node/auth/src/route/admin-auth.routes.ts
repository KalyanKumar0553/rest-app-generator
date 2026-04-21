import { Router } from 'express';

import { AdminAuthController } from '../controller/admin-auth.controller';

export const buildAdminAuthRouter = (adminAuthController: AdminAuthController): Router => {
  const router = Router();
  router.get('/users', adminAuthController.users);
  router.get('/providers', adminAuthController.providers);
  return router;
};
