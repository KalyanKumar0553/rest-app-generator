package com.src.main.subscription.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.src.main.subscription.enums.BillingCycle;
import com.src.main.subscription.enums.SubscriptionStatus;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SubscriptionContextResponse {
	private Long tenantId;
	private String planCode;
	private SubscriptionStatus subscriptionStatus;
	private BillingCycle billingCycle;
	private LocalDateTime expiresAt;
	private Boolean isTrial;
	private List<EntitlementValueResponse> entitlements;
}
