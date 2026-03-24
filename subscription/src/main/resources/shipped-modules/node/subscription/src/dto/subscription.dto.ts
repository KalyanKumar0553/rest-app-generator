export type PlanDto = {
  code: string;
  name: string;
  monthlyPrice: number;
};

export type SubscriptionContextDto = {
  tenantId: string;
  planCode: string;
  status: 'ACTIVE' | 'TRIAL';
  currency: string;
};
