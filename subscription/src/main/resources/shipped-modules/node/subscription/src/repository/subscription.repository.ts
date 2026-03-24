import { prisma } from '../../../../src/lib/prisma';

import type { PlanModel, TenantSubscriptionModel } from '../model/subscription.model';

export class SubscriptionRepository {
  async seedPlans(plans: PlanModel[]): Promise<void> {
    for (const plan of plans) {
      await prisma.subscriptionPlan.upsert({
        where: {
          code: plan.code
        },
        update: {
          name: plan.name,
          monthlyPrice: plan.monthlyPrice
        },
        create: {
          code: plan.code,
          name: plan.name,
          monthlyPrice: plan.monthlyPrice
        }
      });
    }
  }

  async listPlans(): Promise<PlanModel[]> {
    const plans = await prisma.subscriptionPlan.findMany({
      orderBy: {
        monthlyPrice: 'asc'
      }
    });
    return plans.map((plan) => ({
      code: plan.code,
      name: plan.name,
      monthlyPrice: plan.monthlyPrice
    }));
  }

  async subscribe(tenantId: string, planCode: string): Promise<TenantSubscriptionModel> {
    const plan = await prisma.subscriptionPlan.findUnique({
      where: {
        code: planCode
      }
    });
    if (!plan) {
      throw new Error(`Plan ${planCode} not found.`);
    }
    const subscription = await prisma.tenantSubscription.upsert({
      where: {
        tenantId
      },
      update: {
        planCode,
        status: 'ACTIVE'
      },
      create: {
        tenantId,
        planCode,
        status: 'ACTIVE'
      }
    });
    return {
      tenantId: subscription.tenantId,
      planCode: subscription.planCode,
      status: subscription.status as TenantSubscriptionModel['status']
    };
  }

  async currentSubscription(tenantId: string, defaultPlanCode: string): Promise<TenantSubscriptionModel> {
    const subscription = await prisma.tenantSubscription.findUnique({
      where: {
        tenantId
      }
    });
    if (!subscription) {
      return {
        tenantId,
        planCode: defaultPlanCode,
        status: 'TRIAL'
      };
    }
    return {
      tenantId: subscription.tenantId,
      planCode: subscription.planCode,
      status: subscription.status as TenantSubscriptionModel['status']
    };
  }
}
