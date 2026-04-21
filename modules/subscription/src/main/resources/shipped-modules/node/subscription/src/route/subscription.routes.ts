import { Router } from 'express';

import { SubscriptionController } from '../controller/subscription.controller';

export const buildSubscriptionRouter = (subscriptionController: SubscriptionController): Router => {
  const router = Router();
  router.get('/plans', subscriptionController.plans);
  router.get('/current', subscriptionController.current);
  router.post('/subscribe', subscriptionController.subscribe);
  return router;
};
