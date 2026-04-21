package com.src.main.subscription.service;

import java.util.List;

import com.src.main.subscription.dto.SubscriptionAuditCommand;
import com.src.main.subscription.dto.SubscriptionAuditResponse;

public interface SubscriptionAuditService {
	void logEvent(SubscriptionAuditCommand command);
	List<SubscriptionAuditResponse> getAuditHistory(Long tenantId);
}
