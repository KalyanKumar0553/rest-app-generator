package com.src.main.subscription.service.impl;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.src.main.subscription.dto.EntitlementValueResponse;
import com.src.main.subscription.dto.SubscriptionContextResponse;
import com.src.main.subscription.entity.CustomerFeatureOverrideEntity;
import com.src.main.subscription.entity.CustomerSubscriptionEntity;
import com.src.main.subscription.entity.FeatureUsageEntity;
import com.src.main.subscription.entity.PlanFeatureMappingEntity;
import com.src.main.subscription.entity.SubscriptionFeatureEntity;
import com.src.main.subscription.entity.SubscriptionPlanEntity;
import com.src.main.subscription.enums.FeatureType;
import com.src.main.subscription.enums.OverrideType;
import com.src.main.subscription.enums.SubscriptionStatus;
import com.src.main.subscription.repository.CustomerFeatureOverrideRepository;
import com.src.main.subscription.repository.FeatureUsageRepository;
import com.src.main.subscription.repository.PlanFeatureMappingRepository;
import com.src.main.subscription.repository.SubscriptionFeatureRepository;
import com.src.main.subscription.service.EntitlementService;
import com.src.main.subscription.util.SubscriptionMapperUtil;
import com.src.main.subscription.util.SubscriptionPeriodUtil;

@Service
public class EntitlementServiceImpl implements EntitlementService {
	private final SubscriptionLookupService lookupService;
	private final SubscriptionFeatureRepository featureRepository;
	private final PlanFeatureMappingRepository planFeatureRepository;
	private final CustomerFeatureOverrideRepository overrideRepository;
	private final FeatureUsageRepository featureUsageRepository;

	@Override
	@Transactional(readOnly = true)
	public boolean hasFeature(Long tenantId, String featureCode) {
		EntitlementValueResponse response = getEntitlement(tenantId, featureCode);
		return Boolean.TRUE.equals(response.getEnabled());
	}

	@Override
	@Transactional(readOnly = true)
	public long getLimit(Long tenantId, String featureCode) {
		EntitlementValueResponse response = getEntitlement(tenantId, featureCode);
		return response.getLimitValue() == null ? 0L : response.getLimitValue();
	}

	@Override
	@Transactional(readOnly = true)
	public EntitlementValueResponse getEntitlement(Long tenantId, String featureCode) {
		String normalizedCode = featureCode == null ? null : featureCode.trim().toUpperCase();
		EntitlementValueResponse response = getAllEntitlements(tenantId).get(normalizedCode);
		if (response == null) {
			throw new com.src.main.subscription.exception.FeatureNotFoundException(featureCode);
		}
		return response;
	}

	@Override
	@Transactional(readOnly = true)
	@Cacheable(cacheNames = "entitlementsByTenant", key = "#tenantId", sync = true)
	public Map<String, EntitlementValueResponse> getAllEntitlements(Long tenantId) {
		CustomerSubscriptionEntity activeSubscription = lookupService.findActiveSubscription(tenantId);
		SubscriptionPlanEntity plan = activeSubscription == null ? lookupService.getDefaultPlan() : activeSubscription.getPlan();
		List<PlanFeatureMappingEntity> planFeatures = planFeatureRepository.findAllByPlan_IdAndDeletedFalse(plan.getId());
		Map<Long, PlanFeatureMappingEntity> planFeatureByFeatureId = new LinkedHashMap<>();
		planFeatures.forEach(mapping -> planFeatureByFeatureId.put(mapping.getFeature().getId(), mapping));
		Map<Long, CustomerFeatureOverrideEntity> overrideByFeatureId = new LinkedHashMap<>();
		overrideRepository.findAllActiveOverrides(tenantId, LocalDateTime.now()).forEach(override -> overrideByFeatureId.put(override.getFeature().getId(), override));
		Map<String, EntitlementValueResponse> response = new LinkedHashMap<>();
		for (SubscriptionFeatureEntity feature : featureRepository.findAllByIsActiveTrueAndDeletedFalseOrderByNameAsc()) {
			PlanFeatureMappingEntity mapping = planFeatureByFeatureId.get(feature.getId());
			CustomerFeatureOverrideEntity override = overrideByFeatureId.get(feature.getId());
			response.put(feature.getCode(), resolveEntitlement(feature, mapping, override, tenantId, activeSubscription == null ? "DEFAULT_PLAN" : "PLAN"));
		}
		return response;
	}

	@Override
	@Transactional(readOnly = true)
	public SubscriptionContextResponse getSubscriptionContext(Long tenantId) {
		CustomerSubscriptionEntity activeSubscription = lookupService.findActiveSubscription(tenantId);
		String planCode = activeSubscription == null ? lookupService.getDefaultPlan().getCode() : activeSubscription.getPlanCodeSnapshot();
		SubscriptionStatus status = activeSubscription == null ? SubscriptionStatus.ACTIVE : activeSubscription.getStatus();
		return SubscriptionMapperUtil.toSubscriptionContext(tenantId, planCode, status, activeSubscription, getAllEntitlements(tenantId));
	}

	@Override
	@CacheEvict(cacheNames = "entitlementsByTenant", key = "#tenantId")
	public void evictTenantCaches(Long tenantId) {
		lookupService.evictActiveSubscription(tenantId);
	}

	private EntitlementValueResponse resolveEntitlement(SubscriptionFeatureEntity feature, PlanFeatureMappingEntity mapping, CustomerFeatureOverrideEntity override, Long tenantId, String baseSource) {
		boolean enabled = mapping != null && Boolean.TRUE.equals(mapping.getIsEnabled());
		Long limitValue = mapping == null ? 0L : mapping.getLimitValue();
		String source = baseSource;
		if (feature.getFeatureType() == FeatureType.BOOLEAN && mapping == null) {
			limitValue = null;
		}
		if (override != null) {
			source = "PLAN_PLUS_OVERRIDE";
			if (override.getOverrideType() == OverrideType.DISABLE) {
				enabled = false;
				limitValue = feature.getFeatureType() == FeatureType.BOOLEAN ? null : 0L;
			} else if (override.getOverrideType() == OverrideType.REPLACE) {
				if (override.getIsEnabled() != null) {
					enabled = override.getIsEnabled();
				}
				if (override.getLimitValue() != null) {
					limitValue = override.getLimitValue();
				}
			} else if (override.getOverrideType() == OverrideType.ADD) {
				if (override.getIsEnabled() != null) {
					enabled = enabled || override.getIsEnabled();
				}
				if (override.getLimitValue() != null) {
					limitValue = (limitValue == null ? 0L : limitValue) + override.getLimitValue();
				}
			}
		}
		if (feature.getFeatureType() == FeatureType.BOOLEAN) {
			return EntitlementValueResponse.builder().featureCode(feature.getCode()).featureType(feature.getFeatureType()).enabled(enabled).unit(feature.getUnit()).source(source).build();
		}
		if (limitValue == null) {
			limitValue = 0L;
		}
		Long usedValue = null;
		Long remainingValue = null;
		if (feature.getFeatureType() == FeatureType.QUOTA) {
			SubscriptionPeriodUtil.PeriodWindow window = SubscriptionPeriodUtil.resolveWindow(feature, LocalDateTime.now());
			FeatureUsageEntity usage = featureUsageRepository.findByTenantIdAndFeature_IdAndPeriodKey(tenantId, feature.getId(), window.key()).orElse(null);
			usedValue = usage == null ? 0L : usage.getUsedValue();
			remainingValue = Math.max(0L, limitValue - usedValue);
			enabled = enabled && limitValue > 0;
		} else {
			enabled = enabled && limitValue >= 0;
		}
		return EntitlementValueResponse.builder().featureCode(feature.getCode()).featureType(feature.getFeatureType()).enabled(enabled).limitValue(limitValue).usedValue(usedValue).remainingValue(remainingValue).unit(feature.getUnit()).source(source).build();
	}

	public EntitlementServiceImpl(final SubscriptionLookupService lookupService, final SubscriptionFeatureRepository featureRepository, final PlanFeatureMappingRepository planFeatureRepository, final CustomerFeatureOverrideRepository overrideRepository, final FeatureUsageRepository featureUsageRepository) {
		this.lookupService = lookupService;
		this.featureRepository = featureRepository;
		this.planFeatureRepository = planFeatureRepository;
		this.overrideRepository = overrideRepository;
		this.featureUsageRepository = featureUsageRepository;
	}
}
