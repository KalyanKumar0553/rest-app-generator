package com.src.main.subscription.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.src.main.subscription.enums.BillingCycle;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PlanPriceResponse {
	private Long id;
	private Long planId;
	private String planCode;
	private BillingCycle billingCycle;
	private String currencyCode;
	private BigDecimal amount;
	private BigDecimal discountPercent;
	private LocalDateTime effectiveFrom;
	private LocalDateTime effectiveTo;
	private Boolean isActive;
	private String displayLabel;
	private String metadataJson;
}
