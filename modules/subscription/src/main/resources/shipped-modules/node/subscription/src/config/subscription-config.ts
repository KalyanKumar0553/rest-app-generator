export type SubscriptionModuleConfig = {
  currency?: string;
  defaultPlanCode?: string;
  allowTrial?: boolean;
  plans?: Array<{
    code: string;
    name: string;
    description?: string;
    monthlyPrice: number;
    yearlyPrice?: number;
    trialDays?: number;
    features?: string[];
    roles?: string[];
  }>;
};

export const resolveSubscriptionConfig = (config: Record<string, unknown>): Required<SubscriptionModuleConfig> => ({
  currency: typeof config.currency === 'string' && config.currency.trim() ? config.currency.trim() : 'INR',
  defaultPlanCode: typeof config.defaultPlanCode === 'string' && config.defaultPlanCode.trim()
    ? config.defaultPlanCode.trim().toUpperCase()
    : 'FREE',
  allowTrial: typeof config.allowTrial === 'boolean' ? config.allowTrial : true,
  plans: Array.isArray(config.plans) && config.plans.length
    ? config.plans.map((plan) => ({
        code: String((plan as Record<string, unknown>).code ?? 'CUSTOM'),
        name: String((plan as Record<string, unknown>).name ?? 'Custom'),
        description: String((plan as Record<string, unknown>).description ?? '').trim() || undefined,
        monthlyPrice: Number((plan as Record<string, unknown>).monthlyPrice ?? 0),
        yearlyPrice: Number((plan as Record<string, unknown>).yearlyPrice ?? 0),
        trialDays: Number((plan as Record<string, unknown>).trialDays ?? 0),
        features: Array.isArray((plan as Record<string, unknown>).features)
          ? ((plan as Record<string, unknown>).features as unknown[])
              .map((item) => String(item ?? '').trim())
              .filter(Boolean)
          : [],
        roles: Array.isArray((plan as Record<string, unknown>).roles)
          ? ((plan as Record<string, unknown>).roles as unknown[])
              .map((item) => String(item ?? '').trim())
              .filter(Boolean)
          : []
      }))
    : [
        {
          code: 'FREE',
          name: 'Free',
          description: 'Starter plan for evaluation and low-traffic workloads.',
          monthlyPrice: 0,
          yearlyPrice: 0,
          trialDays: 0,
          features: ['core-api'],
          roles: ['ROLE_USER']
        },
        {
          code: 'PRO',
          name: 'Pro',
          description: 'Production plan with team workflows and premium access.',
          monthlyPrice: 499,
          yearlyPrice: 4999,
          trialDays: 14,
          features: ['core-api', 'priority-support', 'analytics'],
          roles: ['ROLE_USER', 'ROLE_MANAGER']
        }
      ]
});
