package com.src.main.subscription.dto;

import com.src.main.subscription.entity.CustomerSubscriptionEntity;
import com.src.main.subscription.entity.SubscriptionPlanEntity;
import com.src.main.subscription.enums.AuditActorType;
import com.src.main.subscription.enums.SubscriptionStatus;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SubscriptionAuditCommand {
	private Long tenantId;
	private CustomerSubscriptionEntity subscription;
	private String eventType;
	private SubscriptionPlanEntity oldPlan;
	private SubscriptionPlanEntity newPlan;
	private SubscriptionStatus oldStatus;
	private SubscriptionStatus newStatus;
	private AuditActorType actorType;
	private String actorId;
	private String reason;
	private String payloadJson;
}
