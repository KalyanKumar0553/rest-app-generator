export type SubscriptionModuleConfig = {
  currency?: string;
  plans?: Array<{
    code: string;
    name: string;
    monthlyPrice: number;
  }>;
};

export const resolveSubscriptionConfig = (config: Record<string, unknown>): Required<SubscriptionModuleConfig> => ({
  currency: typeof config.currency === 'string' && config.currency.trim() ? config.currency.trim() : 'INR',
  plans: Array.isArray(config.plans) && config.plans.length
    ? config.plans.map((plan) => ({
        code: String((plan as Record<string, unknown>).code ?? 'CUSTOM'),
        name: String((plan as Record<string, unknown>).name ?? 'Custom'),
        monthlyPrice: Number((plan as Record<string, unknown>).monthlyPrice ?? 0)
      }))
    : [
        { code: 'FREE', name: 'Free', monthlyPrice: 0 },
        { code: 'PRO', name: 'Pro', monthlyPrice: 499 }
      ]
});
