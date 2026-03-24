package com.src.main.subscription.service;

import java.util.Map;

import com.src.main.subscription.dto.EntitlementValueResponse;
import com.src.main.subscription.dto.SubscriptionContextResponse;

public interface EntitlementService {
	boolean hasFeature(Long tenantId, String featureCode);
	long getLimit(Long tenantId, String featureCode);
	EntitlementValueResponse getEntitlement(Long tenantId, String featureCode);
	Map<String, EntitlementValueResponse> getAllEntitlements(Long tenantId);
	SubscriptionContextResponse getSubscriptionContext(Long tenantId);
	void evictTenantCaches(Long tenantId);
}
