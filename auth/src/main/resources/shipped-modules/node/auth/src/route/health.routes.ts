import { Router } from 'express';

import { HealthController } from '../controller/health.controller';

export const buildHealthRouter = (healthController: HealthController): Router => {
  const router = Router();
  router.get('/health', healthController.health);
  return router;
};
