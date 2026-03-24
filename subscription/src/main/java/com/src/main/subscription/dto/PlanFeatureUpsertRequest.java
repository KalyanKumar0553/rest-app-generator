package com.src.main.subscription.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PlanFeatureUpsertRequest {
	@NotBlank
	private String featureCode;
	private Boolean isEnabled = Boolean.FALSE;
	private Long limitValue;
	private BigDecimal decimalValue;
	private String stringValue;
	private String metadataJson;
}
