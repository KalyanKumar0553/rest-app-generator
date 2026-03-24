package com.src.main.subscription.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CancelSubscriptionRequest {
	@NotNull
	private Long tenantId;
	private Boolean immediate = Boolean.FALSE;
	private String reason;
}
