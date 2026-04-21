package com.src.main.subscription.service.impl;

import java.time.LocalDateTime;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.src.main.subscription.dto.EntitlementValueResponse;
import com.src.main.subscription.dto.UsageConsumeResponse;
import com.src.main.subscription.dto.UsageStatusResponse;
import com.src.main.subscription.entity.FeatureUsageEntity;
import com.src.main.subscription.entity.SubscriptionFeatureEntity;
import com.src.main.subscription.enums.FeatureType;
import com.src.main.subscription.exception.InvalidSubscriptionOperationException;
import com.src.main.subscription.exception.QuotaExceededException;
import com.src.main.subscription.repository.FeatureUsageRepository;
import com.src.main.subscription.service.EntitlementService;
import com.src.main.subscription.service.UsageTrackingService;
import com.src.main.subscription.util.SubscriptionPeriodUtil;

@Service
public class UsageTrackingServiceImpl implements UsageTrackingService {
	private final FeatureUsageRepository featureUsageRepository;
	private final SubscriptionLookupService lookupService;
	private final EntitlementService entitlementService;

	@Override
	@Transactional(readOnly = true)
	public UsageStatusResponse getUsage(Long tenantId, String featureCode) {
		SubscriptionFeatureEntity feature = lookupService.getFeatureByCode(featureCode);
		if (feature.getFeatureType() != FeatureType.QUOTA) {
			throw new InvalidSubscriptionOperationException("Usage is supported only for QUOTA features");
		}
		SubscriptionPeriodUtil.PeriodWindow window = SubscriptionPeriodUtil.resolveWindow(feature, LocalDateTime.now());
		FeatureUsageEntity usage = featureUsageRepository.findByTenantIdAndFeature_IdAndPeriodKey(tenantId, feature.getId(), window.key()).orElse(null);
		EntitlementValueResponse entitlement = entitlementService.getEntitlement(tenantId, feature.getCode());
		long used = usage == null ? 0L : usage.getUsedValue();
		long reserved = usage == null ? 0L : usage.getReservedValue();
		return UsageStatusResponse.builder().tenantId(tenantId).featureCode(feature.getCode()).usedValue(used).reservedValue(reserved).remainingValue(entitlement.getRemainingValue()).periodKey(window.key()).build();
	}

	@Override
	@Transactional
	@CacheEvict(cacheNames = "entitlementsByTenant", key = "#tenantId")
	public UsageConsumeResponse consume(Long tenantId, String featureCode, long units, String reason) {
		if (units <= 0) {
			throw new InvalidSubscriptionOperationException("Usage units must be positive");
		}
		SubscriptionFeatureEntity feature = lookupService.getFeatureByCode(featureCode);
		if (feature.getFeatureType() != FeatureType.QUOTA) {
			throw new InvalidSubscriptionOperationException("Usage consume is supported only for QUOTA features");
		}
		EntitlementValueResponse entitlement = entitlementService.getEntitlement(tenantId, feature.getCode());
		long remaining = entitlement.getRemainingValue() == null ? 0L : entitlement.getRemainingValue();
		if (!Boolean.TRUE.equals(entitlement.getEnabled()) || remaining < units) {
			throw new QuotaExceededException(feature.getCode());
		}
		SubscriptionPeriodUtil.PeriodWindow window = SubscriptionPeriodUtil.resolveWindow(feature, LocalDateTime.now());
		FeatureUsageEntity usage = featureUsageRepository.findByTenantIdAndFeature_IdAndPeriodKey(tenantId, feature.getId(), window.key()).orElseGet(() -> {
			FeatureUsageEntity entity = new FeatureUsageEntity();
			entity.setTenantId(tenantId);
			entity.setFeature(feature);
			entity.setPeriodKey(window.key());
			entity.setPeriodStart(window.start());
			entity.setPeriodEnd(window.end());
			return entity;
		});
		usage.setUsedValue((usage.getUsedValue() == null ? 0L : usage.getUsedValue()) + units);
		usage.setLastConsumedAt(LocalDateTime.now());
		featureUsageRepository.save(usage);
		return UsageConsumeResponse.builder().allowed(Boolean.TRUE).usedValue(usage.getUsedValue()).remainingValue(Math.max(0L, entitlement.getLimitValue() - usage.getUsedValue())).featureCode(feature.getCode()).build();
	}

	@Override
	@Transactional
	@CacheEvict(cacheNames = "entitlementsByTenant", key = "#tenantId")
	public void release(Long tenantId, String featureCode, long units, String reason) {
		if (units <= 0) {
			throw new InvalidSubscriptionOperationException("Usage units must be positive");
		}
		SubscriptionFeatureEntity feature = lookupService.getFeatureByCode(featureCode);
		SubscriptionPeriodUtil.PeriodWindow window = SubscriptionPeriodUtil.resolveWindow(feature, LocalDateTime.now());
		FeatureUsageEntity usage = featureUsageRepository.findByTenantIdAndFeature_IdAndPeriodKey(tenantId, feature.getId(), window.key()).orElse(null);
		if (usage == null) {
			return;
		}
		usage.setUsedValue(Math.max(0L, usage.getUsedValue() - units));
		featureUsageRepository.save(usage);
	}

	@Override
	@Transactional(readOnly = true)
	public boolean hasRemainingQuota(Long tenantId, String featureCode, long requestedUnits) {
		EntitlementValueResponse response = entitlementService.getEntitlement(tenantId, featureCode);
		return Boolean.TRUE.equals(response.getEnabled()) && response.getRemainingValue() != null && response.getRemainingValue() >= requestedUnits;
	}

	public UsageTrackingServiceImpl(final FeatureUsageRepository featureUsageRepository, final SubscriptionLookupService lookupService, final EntitlementService entitlementService) {
		this.featureUsageRepository = featureUsageRepository;
		this.lookupService = lookupService;
		this.entitlementService = entitlementService;
	}
}
