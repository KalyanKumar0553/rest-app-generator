package com.src.main.subscription.util;

import java.util.List;
import java.util.Map;

import com.src.main.subscription.dto.EntitlementValueResponse;
import com.src.main.subscription.dto.FeatureResponse;
import com.src.main.subscription.dto.PlanFeatureResponse;
import com.src.main.subscription.dto.PlanPriceResponse;
import com.src.main.subscription.dto.PlanResponse;
import com.src.main.subscription.dto.SubscriptionAuditResponse;
import com.src.main.subscription.dto.SubscriptionContextResponse;
import com.src.main.subscription.dto.SubscriptionOverrideResponse;
import com.src.main.subscription.dto.SubscriptionResponse;
import com.src.main.subscription.entity.CustomerFeatureOverrideEntity;
import com.src.main.subscription.entity.CustomerSubscriptionEntity;
import com.src.main.subscription.entity.PlanFeatureMappingEntity;
import com.src.main.subscription.entity.PlanPriceEntity;
import com.src.main.subscription.entity.SubscriptionAuditLogEntity;
import com.src.main.subscription.entity.SubscriptionFeatureEntity;
import com.src.main.subscription.entity.SubscriptionPlanEntity;
import com.src.main.subscription.enums.SubscriptionStatus;

public final class SubscriptionMapperUtil {

	private SubscriptionMapperUtil() {
	}

	public static PlanResponse toPlanResponse(SubscriptionPlanEntity entity) {
		return PlanResponse.builder()
				.id(entity.getId())
				.code(entity.getCode())
				.name(entity.getName())
				.description(entity.getDescription())
				.isActive(entity.getIsActive())
				.isDefault(entity.getIsDefault())
				.sortOrder(entity.getSortOrder())
				.trialDays(entity.getTrialDays())
				.planType(entity.getPlanType())
				.visibility(entity.getVisibility())
				.maxUsers(entity.getMaxUsers())
				.maxProjects(entity.getMaxProjects())
				.maxStorageMb(entity.getMaxStorageMb())
				.metadataJson(entity.getMetadataJson())
				.createdAt(entity.getCreatedAt())
				.updatedAt(entity.getUpdatedAt())
				.build();
	}

	public static FeatureResponse toFeatureResponse(SubscriptionFeatureEntity entity) {
		return FeatureResponse.builder()
				.id(entity.getId())
				.code(entity.getCode())
				.name(entity.getName())
				.description(entity.getDescription())
				.featureType(entity.getFeatureType())
				.valueDataType(entity.getValueDataType())
				.unit(entity.getUnit())
				.resetPolicy(entity.getResetPolicy())
				.isActive(entity.getIsActive())
				.isSystem(entity.getIsSystem())
				.metadataJson(entity.getMetadataJson())
				.createdAt(entity.getCreatedAt())
				.updatedAt(entity.getUpdatedAt())
				.build();
	}

	public static PlanFeatureResponse toPlanFeatureResponse(PlanFeatureMappingEntity entity) {
		return PlanFeatureResponse.builder()
				.id(entity.getId())
				.planId(entity.getPlan().getId())
				.planCode(entity.getPlan().getCode())
				.featureId(entity.getFeature().getId())
				.featureCode(entity.getFeature().getCode())
				.featureType(entity.getFeature().getFeatureType())
				.isEnabled(entity.getIsEnabled())
				.limitValue(entity.getLimitValue())
				.decimalValue(entity.getDecimalValue())
				.stringValue(entity.getStringValue())
				.metadataJson(entity.getMetadataJson())
				.build();
	}

	public static PlanPriceResponse toPlanPriceResponse(PlanPriceEntity entity) {
		return PlanPriceResponse.builder()
				.id(entity.getId())
				.planId(entity.getPlan().getId())
				.planCode(entity.getPlan().getCode())
				.billingCycle(entity.getBillingCycle())
				.currencyCode(entity.getCurrencyCode())
				.amount(entity.getAmount())
				.discountPercent(entity.getDiscountPercent())
				.effectiveFrom(entity.getEffectiveFrom())
				.effectiveTo(entity.getEffectiveTo())
				.isActive(entity.getIsActive())
				.displayLabel(entity.getDisplayLabel())
				.metadataJson(entity.getMetadataJson())
				.build();
	}

	public static SubscriptionResponse toSubscriptionResponse(CustomerSubscriptionEntity entity) {
		return SubscriptionResponse.builder()
				.subscriptionId(entity.getId())
				.tenantId(entity.getTenantId())
				.planCode(entity.getPlanCodeSnapshot())
				.planName(entity.getPlan().getName())
				.billingCycle(entity.getBillingCycle())
				.status(entity.getStatus())
				.startAt(entity.getStartAt())
				.endAt(entity.getEndAt())
				.trialStartAt(entity.getTrialStartAt())
				.trialEndAt(entity.getTrialEndAt())
				.autoRenew(entity.getAutoRenew())
				.priceSnapshot(entity.getPriceSnapshot())
				.currencyCode(entity.getCurrencyCode())
				.build();
	}

	public static SubscriptionOverrideResponse toOverrideResponse(CustomerFeatureOverrideEntity entity) {
		return SubscriptionOverrideResponse.builder()
				.id(entity.getId())
				.tenantId(entity.getTenantId())
				.featureCode(entity.getFeature().getCode())
				.isEnabled(entity.getIsEnabled())
				.limitValue(entity.getLimitValue())
				.decimalValue(entity.getDecimalValue())
				.stringValue(entity.getStringValue())
				.overrideType(entity.getOverrideType())
				.reason(entity.getReason())
				.effectiveFrom(entity.getEffectiveFrom())
				.effectiveTo(entity.getEffectiveTo())
				.isActive(entity.getIsActive())
				.metadataJson(entity.getMetadataJson())
				.build();
	}

	public static SubscriptionAuditResponse toAuditResponse(SubscriptionAuditLogEntity entity) {
		return SubscriptionAuditResponse.builder()
				.id(entity.getId())
				.tenantId(entity.getTenantId())
				.subscriptionId(entity.getSubscription() == null ? null : entity.getSubscription().getId())
				.eventType(entity.getEventType())
				.oldPlanCode(entity.getOldPlan() == null ? null : entity.getOldPlan().getCode())
				.newPlanCode(entity.getNewPlan() == null ? null : entity.getNewPlan().getCode())
				.oldStatus(entity.getOldStatus())
				.newStatus(entity.getNewStatus())
				.actorType(entity.getActorType())
				.actorId(entity.getActorId())
				.reason(entity.getReason())
				.payloadJson(entity.getPayloadJson())
				.createdAt(entity.getCreatedAt())
				.build();
	}

	public static SubscriptionContextResponse toSubscriptionContext(
			Long tenantId,
			String planCode,
			SubscriptionStatus status,
			CustomerSubscriptionEntity activeSubscription,
			Map<String, EntitlementValueResponse> entitlements) {
		return SubscriptionContextResponse.builder()
				.tenantId(tenantId)
				.planCode(planCode)
				.subscriptionStatus(status)
				.billingCycle(activeSubscription == null ? null : activeSubscription.getBillingCycle())
				.expiresAt(activeSubscription == null ? null : activeSubscription.getEndAt())
				.isTrial(activeSubscription != null && SubscriptionStatus.TRIAL == activeSubscription.getStatus())
				.entitlements(List.copyOf(entitlements.values()))
				.build();
	}
}
