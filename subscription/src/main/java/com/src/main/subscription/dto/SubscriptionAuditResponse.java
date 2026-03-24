package com.src.main.subscription.dto;

import java.time.LocalDateTime;

import com.src.main.subscription.enums.AuditActorType;
import com.src.main.subscription.enums.SubscriptionStatus;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SubscriptionAuditResponse {
	private Long id;
	private Long tenantId;
	private Long subscriptionId;
	private String eventType;
	private String oldPlanCode;
	private String newPlanCode;
	private SubscriptionStatus oldStatus;
	private SubscriptionStatus newStatus;
	private AuditActorType actorType;
	private String actorId;
	private String reason;
	private String payloadJson;
	private LocalDateTime createdAt;
}
