package com.src.main.subscription.service;

import java.time.LocalDateTime;
import java.util.List;

import com.src.main.subscription.dto.CancelSubscriptionRequest;
import com.src.main.subscription.dto.DowngradeSubscriptionRequest;
import com.src.main.subscription.dto.RenewSubscriptionRequest;
import com.src.main.subscription.dto.StartTrialRequest;
import com.src.main.subscription.dto.SubscriptionRequest;
import com.src.main.subscription.dto.SubscriptionResponse;
import com.src.main.subscription.dto.UpgradeSubscriptionRequest;

public interface SubscriptionManagementService {
	SubscriptionResponse assignDefaultPlan(Long tenantId);
	SubscriptionResponse startTrial(StartTrialRequest request);
	SubscriptionResponse subscribe(SubscriptionRequest request);
	SubscriptionResponse upgrade(UpgradeSubscriptionRequest request);
	SubscriptionResponse scheduleDowngrade(DowngradeSubscriptionRequest request);
	SubscriptionResponse cancel(CancelSubscriptionRequest request);
	SubscriptionResponse renew(RenewSubscriptionRequest request);
	void expireSubscriptions(LocalDateTime now);
	SubscriptionResponse getCurrentSubscription(Long tenantId);
	List<SubscriptionResponse> getSubscriptionHistory(Long tenantId);
}
