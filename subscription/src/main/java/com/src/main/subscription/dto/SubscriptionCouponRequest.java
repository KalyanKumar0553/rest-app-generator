package com.src.main.subscription.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.src.main.subscription.enums.DiscountType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SubscriptionCouponRequest {
	@NotBlank
	private String code;
	@NotBlank
	private String name;
	private String description;
	private Boolean isActive = Boolean.TRUE;
	@NotNull
	private DiscountType discountType;
	@NotNull
	private BigDecimal discountValue;
	private String currencyCode;
	@NotNull
	private LocalDateTime validFrom;
	private LocalDateTime validTo;
	private Integer maxRedemptions;
	private Integer maxRedemptionsPerTenant;
	private Boolean firstSubscriptionOnly = Boolean.FALSE;
	private List<Long> applicablePlanIds;
	private String metadataJson;
}
