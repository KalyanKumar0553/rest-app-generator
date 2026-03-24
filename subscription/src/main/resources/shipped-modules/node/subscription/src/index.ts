import type { Express } from 'express';

import { resolveSubscriptionConfig } from './config/subscription-config';
import { SubscriptionController } from './controller/subscription.controller';
import { SubscriptionRepository } from './repository/subscription.repository';
import { buildSubscriptionRouter } from './route/subscription.routes';
import { SubscriptionService } from './service/subscription.service';

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
  const resolvedConfig = resolveSubscriptionConfig(config);
  const subscriptionRepository = new SubscriptionRepository();
  const subscriptionService = new SubscriptionService(resolvedConfig, subscriptionRepository);
  const subscriptionController = new SubscriptionController(subscriptionService, resolvedConfig);
  app.use('/api/modules/subscription', buildSubscriptionRouter(subscriptionController));
};
