export type PlanModel = {
  code: string;
  name: string;
  monthlyPrice: number;
};

export type TenantSubscriptionModel = {
  tenantId: string;
  planCode: string;
  status: 'ACTIVE' | 'TRIAL';
};
