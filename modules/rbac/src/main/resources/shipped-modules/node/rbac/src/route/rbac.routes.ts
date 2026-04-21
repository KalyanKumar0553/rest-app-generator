import { Router } from 'express';

import { RbacController } from '../controller/rbac.controller';

export const buildRbacRouter = (rbacController: RbacController): Router => {
  const router = Router();
  router.get('/roles', rbacController.roles);
  router.post('/assign-role', rbacController.assignRole);
  return router;
};
