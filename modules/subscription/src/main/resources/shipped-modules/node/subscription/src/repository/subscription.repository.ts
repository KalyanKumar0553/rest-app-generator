import { prisma } from '../../../../src/lib/prisma';

import type { PlanModel, TenantSubscriptionModel } from '../model/subscription.model';

export class SubscriptionRepository {
  async seedPlans(plans: PlanModel[]): Promise<void> {
    for (const plan of plans) {
      const persistedPlan = await prisma.subscriptionPlan.upsert({
        where: {
          code: plan.code
        },
        update: {
          name: plan.name,
          description: plan.description ?? null,
          monthlyPrice: plan.monthlyPrice,
          yearlyPrice: plan.yearlyPrice ?? null,
          trialDays: plan.trialDays ?? 0
        },
        create: {
          code: plan.code,
          name: plan.name,
          description: plan.description ?? null,
          monthlyPrice: plan.monthlyPrice,
          yearlyPrice: plan.yearlyPrice ?? null,
          trialDays: plan.trialDays ?? 0
        }
      });
      if (Array.isArray(plan.features)) {
        for (const [index, featureCode] of plan.features.entries()) {
          const normalizedFeatureCode = String(featureCode ?? '').trim();
          if (!normalizedFeatureCode) {
            continue;
          }
          const feature = await prisma.subscriptionFeature.upsert({
            where: {
              code: normalizedFeatureCode
            },
            update: {
              name: normalizedFeatureCode.replace(/[-_]/g, ' ')
            },
            create: {
              code: normalizedFeatureCode,
              name: normalizedFeatureCode.replace(/[-_]/g, ' ')
            }
          });
          await prisma.planFeatureMapping.upsert({
            where: {
              planId_featureId: {
                planId: persistedPlan.id,
                featureId: feature.id
              }
            },
            update: {
              isEnabled: true,
              limitValue: index + 1
            },
            create: {
              planId: persistedPlan.id,
              featureId: feature.id,
              isEnabled: true,
              limitValue: index + 1
            }
          });
        }
      }
      if (Array.isArray(plan.roles)) {
        for (const roleName of plan.roles) {
          const normalizedRole = String(roleName ?? '').trim();
          if (!normalizedRole) {
            continue;
          }
          await prisma.subscriptionPlanRoleMapping.upsert({
            where: {
              planId_roleName: {
                planId: persistedPlan.id,
                roleName: normalizedRole
              }
            },
            update: {
              roleName: normalizedRole
            },
            create: {
              planId: persistedPlan.id,
              roleName: normalizedRole
            }
          });
        }
      }
    }
  }

  async listPlans(): Promise<PlanModel[]> {
    const plans = await prisma.subscriptionPlan.findMany({
      include: {
        featureMappings: {
          include: {
            feature: true
          }
        },
        roleMappings: true
      },
      orderBy: {
        monthlyPrice: 'asc'
      }
    });
    return plans.map((plan) => ({
      code: plan.code,
      name: plan.name,
      description: plan.description ?? undefined,
      monthlyPrice: plan.monthlyPrice,
      yearlyPrice: plan.yearlyPrice ?? undefined,
      trialDays: plan.trialDays ?? undefined,
      features: plan.featureMappings.map((mapping) => mapping.feature.code),
      roles: plan.roleMappings.map((mapping) => mapping.roleName)
    }));
  }

  async subscribe(tenantId: string, planCode: string, allowTrial: boolean, currency: string): Promise<TenantSubscriptionModel> {
    const plan = await prisma.subscriptionPlan.findUnique({
      where: {
        code: planCode
      }
    });
    if (!plan) {
      throw new Error(`Plan ${planCode} not found.`);
    }
    const featureMappings = await prisma.planFeatureMapping.findMany({
      where: {
        planId: plan.id,
        isEnabled: true
      },
      include: {
        feature: true
      }
    });
    const roleMappings = await prisma.subscriptionPlanRoleMapping.findMany({
      where: {
        planId: plan.id
      }
    });
    const status = allowTrial && (plan.trialDays ?? 0) > 0 ? 'TRIAL' : 'ACTIVE';
    const subscription = await prisma.tenantSubscription.upsert({
      where: {
        tenantId
      },
      update: {
        planCode,
        status,
        currency
      },
      create: {
        tenantId,
        planCode,
        status,
        currency
      }
    });
    await prisma.subscriptionAuditLog.create({
      data: {
        tenantId,
        subscriptionId: subscription.tenantId,
        eventType: subscription.status === 'TRIAL' ? 'SUBSCRIPTION_TRIAL_STARTED' : 'SUBSCRIPTION_ACTIVATED',
        reason: `Tenant subscribed to ${plan.code}.`
      }
    });
    return {
      tenantId: subscription.tenantId,
      planCode: subscription.planCode,
      status: subscription.status as TenantSubscriptionModel['status'],
      currency: subscription.currency ?? currency,
      features: featureMappings.map((mapping) => mapping.feature.code),
      roles: roleMappings.map((mapping) => mapping.roleName)
    };
  }

  async currentSubscription(tenantId: string, defaultPlanCode: string, currency: string, allowTrial: boolean): Promise<TenantSubscriptionModel> {
    const subscription = await prisma.tenantSubscription.findUnique({
      where: {
        tenantId
      }
    });
    if (!subscription) {
      const plan = await prisma.subscriptionPlan.findUnique({
        where: {
          code: defaultPlanCode
        },
        include: {
          featureMappings: {
            include: {
              feature: true
            }
          },
          roleMappings: true
        }
      });
      return {
        tenantId,
        planCode: defaultPlanCode,
        status: allowTrial ? 'TRIAL' : 'ACTIVE',
        currency,
        features: plan?.featureMappings.map((mapping) => mapping.feature.code) ?? [],
        roles: plan?.roleMappings.map((mapping) => mapping.roleName) ?? []
      };
    }
    const plan = await prisma.subscriptionPlan.findUnique({
      where: {
        code: subscription.planCode
      },
      include: {
        featureMappings: {
          include: {
            feature: true
          }
        },
        roleMappings: true
      }
    });
    return {
      tenantId: subscription.tenantId,
      planCode: subscription.planCode,
      status: subscription.status as TenantSubscriptionModel['status'],
      currency: subscription.currency ?? currency,
      features: plan?.featureMappings.map((mapping) => mapping.feature.code) ?? [],
      roles: plan?.roleMappings.map((mapping) => mapping.roleName) ?? []
    };
  }
}
