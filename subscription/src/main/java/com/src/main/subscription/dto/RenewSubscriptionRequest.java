package com.src.main.subscription.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RenewSubscriptionRequest {
	@NotNull
	private Long tenantId;
	private String reason;
}
