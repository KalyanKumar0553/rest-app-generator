package com.src.main.subscription.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.src.main.subscription.dto.SubscriptionOverrideRequest;
import com.src.main.subscription.dto.SubscriptionOverrideResponse;
import com.src.main.subscription.entity.CustomerFeatureOverrideEntity;
import com.src.main.subscription.entity.SubscriptionFeatureEntity;
import com.src.main.subscription.exception.InvalidSubscriptionOperationException;
import com.src.main.subscription.repository.CustomerFeatureOverrideRepository;
import com.src.main.subscription.service.SubscriptionOverrideService;
import com.src.main.subscription.util.SubscriptionMapperUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SubscriptionOverrideServiceImpl implements SubscriptionOverrideService {

	private final CustomerFeatureOverrideRepository overrideRepository;
	private final SubscriptionLookupService lookupService;

	@Override
	@Transactional
	@CacheEvict(cacheNames = "entitlementsByTenant", key = "#tenantId")
	public SubscriptionOverrideResponse createOverride(Long tenantId, SubscriptionOverrideRequest request) {
		CustomerFeatureOverrideEntity entity = new CustomerFeatureOverrideEntity();
		apply(entity, tenantId, request);
		return SubscriptionMapperUtil.toOverrideResponse(overrideRepository.save(entity));
	}

	@Override
	@Transactional
	@CacheEvict(cacheNames = "entitlementsByTenant", key = "#tenantId")
	public SubscriptionOverrideResponse updateOverride(Long tenantId, Long overrideId, SubscriptionOverrideRequest request) {
		CustomerFeatureOverrideEntity entity = overrideRepository.findById(overrideId)
				.filter(existing -> Boolean.FALSE.equals(existing.getDeleted()) && tenantId.equals(existing.getTenantId()))
				.orElseThrow(() -> new InvalidSubscriptionOperationException("Subscription override not found: " + overrideId));
		apply(entity, tenantId, request);
		return SubscriptionMapperUtil.toOverrideResponse(overrideRepository.save(entity));
	}

	@Override
	@Transactional(readOnly = true)
	public List<SubscriptionOverrideResponse> getOverrides(Long tenantId) {
		return overrideRepository.findAllByTenantIdAndDeletedFalseOrderByEffectiveFromDesc(tenantId).stream()
				.map(SubscriptionMapperUtil::toOverrideResponse)
				.toList();
	}

	@Override
	@Transactional
	@CacheEvict(cacheNames = "entitlementsByTenant", key = "#tenantId")
	public void deleteOverride(Long tenantId, Long overrideId) {
		CustomerFeatureOverrideEntity entity = overrideRepository.findById(overrideId)
				.filter(existing -> Boolean.FALSE.equals(existing.getDeleted()) && tenantId.equals(existing.getTenantId()))
				.orElseThrow(() -> new InvalidSubscriptionOperationException("Subscription override not found: " + overrideId));
		entity.setDeleted(Boolean.TRUE);
		overrideRepository.save(entity);
	}

	private void apply(CustomerFeatureOverrideEntity entity, Long tenantId, SubscriptionOverrideRequest request) {
		SubscriptionFeatureEntity feature = lookupService.getFeatureByCode(request.getFeatureCode());
		if (request.getEffectiveTo() != null && request.getEffectiveTo().isBefore(request.getEffectiveFrom())) {
			throw new InvalidSubscriptionOperationException("effectiveTo must be after effectiveFrom");
		}
		entity.setTenantId(tenantId);
		entity.setFeature(feature);
		entity.setIsEnabled(request.getIsEnabled());
		entity.setLimitValue(request.getLimitValue());
		entity.setDecimalValue(request.getDecimalValue());
		entity.setStringValue(trimToNull(request.getStringValue()));
		entity.setOverrideType(request.getOverrideType());
		entity.setReason(trimToNull(request.getReason()));
		entity.setEffectiveFrom(request.getEffectiveFrom() == null ? LocalDateTime.now() : request.getEffectiveFrom());
		entity.setEffectiveTo(request.getEffectiveTo());
		entity.setIsActive(Boolean.TRUE.equals(request.getIsActive()));
		entity.setMetadataJson(trimToNull(request.getMetadataJson()));
		entity.setDeleted(Boolean.FALSE);
	}

	private String trimToNull(String value) {
		return value == null || value.isBlank() ? null : value.trim();
	}
}
