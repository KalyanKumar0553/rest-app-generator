package com.src.main.subscription.dto;

import java.math.BigDecimal;

import com.src.main.subscription.enums.FeatureType;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PlanFeatureResponse {
	private Long id;
	private Long planId;
	private String planCode;
	private Long featureId;
	private String featureCode;
	private FeatureType featureType;
	private Boolean isEnabled;
	private Long limitValue;
	private BigDecimal decimalValue;
	private String stringValue;
	private String metadataJson;
}
