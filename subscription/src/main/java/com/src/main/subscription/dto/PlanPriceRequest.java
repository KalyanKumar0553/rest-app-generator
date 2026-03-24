package com.src.main.subscription.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.src.main.subscription.enums.BillingCycle;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PlanPriceRequest {
	@NotNull
	private BillingCycle billingCycle;
	@NotBlank
	private String currencyCode;
	@NotNull
	private BigDecimal amount;
	private BigDecimal discountPercent;
	@NotNull
	private LocalDateTime effectiveFrom;
	private LocalDateTime effectiveTo;
	private Boolean isActive = Boolean.TRUE;
	private String displayLabel;
	private String metadataJson;
}
