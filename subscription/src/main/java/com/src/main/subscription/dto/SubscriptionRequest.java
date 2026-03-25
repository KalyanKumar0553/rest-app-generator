package com.src.main.subscription.dto;

import java.time.LocalDateTime;

import com.src.main.subscription.enums.BillingCycle;
import com.src.main.subscription.enums.SubscriptionSource;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SubscriptionRequest {
	@NotNull
	private Long tenantId;
	private String userId;
	@NotBlank
	private String planCode;
	@NotNull
	private BillingCycle billingCycle;
	@NotBlank
	private String currencyCode;
	@NotNull
	private SubscriptionSource source;
	private Boolean autoRenew = Boolean.FALSE;
	private LocalDateTime startAt;
	private String couponCode;
	private String externalReference;
	private String metadataJson;
	private String reason;
}
