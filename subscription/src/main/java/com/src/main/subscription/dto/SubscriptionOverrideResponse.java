package com.src.main.subscription.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.src.main.subscription.enums.OverrideType;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SubscriptionOverrideResponse {
	private Long id;
	private Long tenantId;
	private String featureCode;
	private Boolean isEnabled;
	private Long limitValue;
	private BigDecimal decimalValue;
	private String stringValue;
	private OverrideType overrideType;
	private String reason;
	private LocalDateTime effectiveFrom;
	private LocalDateTime effectiveTo;
	private Boolean isActive;
	private String metadataJson;
}
