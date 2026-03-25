package com.src.main.subscription.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StartTrialRequest {
	@NotNull
	private Long tenantId;
	private String userId;
	private String planCode;
	private String reason;
}
