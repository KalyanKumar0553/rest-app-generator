export type PlanModel = {
  code: string;
  name: string;
  description?: string;
  monthlyPrice: number;
  yearlyPrice?: number;
  trialDays?: number;
  features?: string[];
  roles?: string[];
};

export type TenantSubscriptionModel = {
  tenantId: string;
  planCode: string;
  status: 'ACTIVE' | 'TRIAL';
  currency?: string;
  features?: string[];
  roles?: string[];
};
