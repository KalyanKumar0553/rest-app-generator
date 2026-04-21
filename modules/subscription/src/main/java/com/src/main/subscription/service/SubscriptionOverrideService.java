package com.src.main.subscription.service;

import java.util.List;

import com.src.main.subscription.dto.SubscriptionOverrideRequest;
import com.src.main.subscription.dto.SubscriptionOverrideResponse;

public interface SubscriptionOverrideService {
	SubscriptionOverrideResponse createOverride(Long tenantId, SubscriptionOverrideRequest request);
	SubscriptionOverrideResponse updateOverride(Long tenantId, Long overrideId, SubscriptionOverrideRequest request);
	List<SubscriptionOverrideResponse> getOverrides(Long tenantId);
	void deleteOverride(Long tenantId, Long overrideId);
}
