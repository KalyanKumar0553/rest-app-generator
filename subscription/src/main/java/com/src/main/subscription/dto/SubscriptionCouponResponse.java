package com.src.main.subscription.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.src.main.subscription.enums.DiscountType;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SubscriptionCouponResponse {
	private Long id;
	private String code;
	private String name;
	private String description;
	private Boolean isActive;
	private DiscountType discountType;
	private BigDecimal discountValue;
	private String currencyCode;
	private LocalDateTime validFrom;
	private LocalDateTime validTo;
	private Integer maxRedemptions;
	private Integer maxRedemptionsPerTenant;
	private Boolean firstSubscriptionOnly;
	private List<Long> applicablePlanIds;
	private String metadataJson;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
}
