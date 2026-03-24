package com.src.main.subscription.service;

import com.src.main.subscription.dto.UsageConsumeResponse;
import com.src.main.subscription.dto.UsageStatusResponse;

public interface UsageTrackingService {
	UsageStatusResponse getUsage(Long tenantId, String featureCode);
	UsageConsumeResponse consume(Long tenantId, String featureCode, long units, String reason);
	void release(Long tenantId, String featureCode, long units, String reason);
	boolean hasRemainingQuota(Long tenantId, String featureCode, long requestedUnits);
}
