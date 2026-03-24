package com.src.main.subscription.dto;

import com.src.main.subscription.enums.FeatureType;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EntitlementValueResponse {
	private String featureCode;
	private FeatureType featureType;
	private Boolean enabled;
	private Long limitValue;
	private Long usedValue;
	private Long remainingValue;
	private String unit;
	private String source;
}
