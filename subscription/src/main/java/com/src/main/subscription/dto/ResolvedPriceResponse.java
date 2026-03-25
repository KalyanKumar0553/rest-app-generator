package com.src.main.subscription.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.src.main.subscription.enums.BillingCycle;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ResolvedPriceResponse {
	private String planCode;
	private BillingCycle billingCycle;
	private String currencyCode;
	private BigDecimal baseAmount;
	private BigDecimal amount;
	private BigDecimal discountPercent;
	private String couponCode;
	private BigDecimal couponDiscountAmount;
	private String displayLabel;
	private LocalDateTime effectiveFrom;
}
