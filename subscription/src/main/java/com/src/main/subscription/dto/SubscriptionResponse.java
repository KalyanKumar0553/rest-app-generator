package com.src.main.subscription.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.src.main.subscription.enums.BillingCycle;
import com.src.main.subscription.enums.SubscriptionStatus;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SubscriptionResponse {
	private Long subscriptionId;
	private Long tenantId;
	private String subscriberUserId;
	private String planCode;
	private String planName;
	private BillingCycle billingCycle;
	private SubscriptionStatus status;
	private LocalDateTime startAt;
	private LocalDateTime endAt;
	private LocalDateTime trialStartAt;
	private LocalDateTime trialEndAt;
	private Boolean autoRenew;
	private BigDecimal priceSnapshot;
	private String currencyCode;
	private String appliedCouponCode;
	private BigDecimal appliedDiscountAmount;
}
