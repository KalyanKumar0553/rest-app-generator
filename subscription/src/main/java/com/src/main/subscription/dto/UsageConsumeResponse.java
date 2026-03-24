package com.src.main.subscription.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UsageConsumeResponse {
	private Boolean allowed;
	private Long usedValue;
	private Long remainingValue;
	private String featureCode;
}
