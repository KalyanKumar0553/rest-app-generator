package com.src.main.subscription.dto;

import com.src.main.subscription.enums.BillingCycle;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpgradeSubscriptionRequest {
	@NotNull
	private Long tenantId;
	private String userId;
	@NotBlank
	private String targetPlanCode;
	@NotNull
	private BillingCycle billingCycle;
	@NotBlank
	private String currencyCode;
	private String couponCode;
	private String reason;
}
