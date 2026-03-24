package com.src.main.subscription.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.src.main.subscription.enums.OverrideType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SubscriptionOverrideRequest {
	@NotBlank
	private String featureCode;
	private Boolean isEnabled;
	private Long limitValue;
	private BigDecimal decimalValue;
	private String stringValue;
	@NotNull
	private OverrideType overrideType;
	private String reason;
	@NotNull
	private LocalDateTime effectiveFrom;
	private LocalDateTime effectiveTo;
	private Boolean isActive = Boolean.TRUE;
	private String metadataJson;
}
