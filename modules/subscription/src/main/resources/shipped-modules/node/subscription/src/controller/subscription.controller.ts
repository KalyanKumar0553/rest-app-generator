import type { Request, Response } from 'express';

import type { SubscriptionModuleConfig } from '../config/subscription-config';
import { SubscriptionService } from '../service/subscription.service';

export class SubscriptionController {
  constructor(
    private readonly subscriptionService: SubscriptionService,
    private readonly config: Required<SubscriptionModuleConfig>
  ) {}

  plans = async (_request: Request, response: Response): Promise<void> => {
    response.json({
      currency: this.config.currency,
      plans: await this.subscriptionService.listPlans()
    });
  };

  subscribe = async (request: Request, response: Response): Promise<void> => {
    const tenantId = String(request.body?.tenantId ?? '').trim();
    const planCode = String(request.body?.planCode ?? '').trim();
    if (!tenantId || !planCode) {
      response.status(400).json({ message: 'tenantId and planCode are required.' });
      return;
    }
    response.json(await this.subscriptionService.subscribe(tenantId, planCode));
  };

  current = async (request: Request, response: Response): Promise<void> => {
    const tenantId = String(request.query.tenantId ?? 'default-tenant');
    response.json(await this.subscriptionService.currentSubscription(tenantId));
  };
}
