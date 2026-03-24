import type { SubscriptionModuleConfig } from '../config/subscription-config';
import type { PlanModel, TenantSubscriptionModel } from '../model/subscription.model';
import { SubscriptionRepository } from '../repository/subscription.repository';

export class SubscriptionService {
  constructor(
    private readonly config: Required<SubscriptionModuleConfig>,
    private readonly subscriptionRepository: SubscriptionRepository
  ) {}

  async ensureSeedData(): Promise<void> {
    await this.subscriptionRepository.seedPlans(this.config.plans);
  }

  async listPlans(): Promise<PlanModel[]> {
    await this.ensureSeedData();
    return this.subscriptionRepository.listPlans();
  }

  async subscribe(tenantId: string, planCode: string): Promise<TenantSubscriptionModel> {
    await this.ensureSeedData();
    return this.subscriptionRepository.subscribe(tenantId, planCode);
  }

  async currentSubscription(tenantId: string): Promise<TenantSubscriptionModel> {
    await this.ensureSeedData();
    return this.subscriptionRepository.currentSubscription(tenantId, this.config.plans[0]?.code ?? 'FREE');
  }
}
