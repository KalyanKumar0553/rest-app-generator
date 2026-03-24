package com.src.main.subscription.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UsageStatusResponse {
	private Long tenantId;
	private String featureCode;
	private Long usedValue;
	private Long reservedValue;
	private Long remainingValue;
	private String periodKey;
}
