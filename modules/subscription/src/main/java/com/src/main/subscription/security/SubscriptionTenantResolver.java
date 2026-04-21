package com.src.main.subscription.security;

public interface SubscriptionTenantResolver {
	Long resolveTenantId();

	default Long resolveRequiredTenantId() {
		Long tenantId = resolveTenantId();
		if (tenantId == null) {
			throw new IllegalArgumentException("Unable to resolve tenant id");
		}
		return tenantId;
	}
}
