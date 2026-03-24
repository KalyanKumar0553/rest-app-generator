package com.src.main.subscription.dto;

import com.src.main.subscription.enums.BillingCycle;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DowngradeSubscriptionRequest {
	@NotNull
	private Long tenantId;
	@NotBlank
	private String targetPlanCode;
	private BillingCycle billingCycle;
	private String currencyCode;
	private String reason;
}
