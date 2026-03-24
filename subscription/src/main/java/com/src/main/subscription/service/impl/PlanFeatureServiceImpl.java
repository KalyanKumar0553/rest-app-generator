package com.src.main.subscription.service.impl;

import java.util.List;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.src.main.subscription.dto.PlanFeatureResponse;
import com.src.main.subscription.dto.PlanFeatureUpsertRequest;
import com.src.main.subscription.entity.PlanFeatureMappingEntity;
import com.src.main.subscription.entity.SubscriptionFeatureEntity;
import com.src.main.subscription.entity.SubscriptionPlanEntity;
import com.src.main.subscription.enums.FeatureType;
import com.src.main.subscription.exception.InvalidSubscriptionOperationException;
import com.src.main.subscription.repository.PlanFeatureMappingRepository;
import com.src.main.subscription.service.PlanFeatureService;
import com.src.main.subscription.util.SubscriptionMapperUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PlanFeatureServiceImpl implements PlanFeatureService {

	private final PlanFeatureMappingRepository planFeatureRepository;
	private final SubscriptionLookupService lookupService;

	@Override
	@Transactional(readOnly = true)
	public List<PlanFeatureResponse> getPlanFeatures(Long planId) {
		return planFeatureRepository.findAllByPlan_IdAndDeletedFalse(planId).stream()
				.map(SubscriptionMapperUtil::toPlanFeatureResponse)
				.toList();
	}

	@Override
	@Transactional
	@Caching(evict = {
			@CacheEvict(cacheNames = "entitlementsByTenant", allEntries = true)
	})
	public List<PlanFeatureResponse> replacePlanFeatures(Long planId, List<PlanFeatureUpsertRequest> requests) {
		List<PlanFeatureMappingEntity> existingMappings = planFeatureRepository.findAllByPlan_IdAndDeletedFalse(planId);
		existingMappings.forEach(mapping -> mapping.setDeleted(Boolean.TRUE));
		planFeatureRepository.saveAll(existingMappings);
		if (requests != null) {
			for (PlanFeatureUpsertRequest request : requests) {
				upsertPlanFeature(planId, request);
			}
		}
		return getPlanFeatures(planId);
	}

	@Override
	@Transactional
	@Caching(evict = {
			@CacheEvict(cacheNames = "entitlementsByTenant", allEntries = true)
	})
	public PlanFeatureResponse upsertPlanFeature(Long planId, PlanFeatureUpsertRequest request) {
		SubscriptionPlanEntity plan = lookupService.getPlanById(planId);
		SubscriptionFeatureEntity feature = lookupService.getFeatureByCode(request.getFeatureCode());
		validateRequest(feature, request);
		PlanFeatureMappingEntity entity = planFeatureRepository
				.findByPlan_IdAndFeature_IdAndDeletedFalse(planId, feature.getId())
				.orElseGet(PlanFeatureMappingEntity::new);
		entity.setPlan(plan);
		entity.setFeature(feature);
		entity.setDeleted(Boolean.FALSE);
		entity.setIsEnabled(Boolean.TRUE.equals(request.getIsEnabled()));
		entity.setLimitValue(request.getLimitValue());
		entity.setDecimalValue(request.getDecimalValue());
		entity.setStringValue(trimToNull(request.getStringValue()));
		entity.setMetadataJson(trimToNull(request.getMetadataJson()));
		return SubscriptionMapperUtil.toPlanFeatureResponse(planFeatureRepository.save(entity));
	}

	@Override
	@Transactional
	@Caching(evict = {
			@CacheEvict(cacheNames = "entitlementsByTenant", allEntries = true)
	})
	public void removePlanFeature(Long planId, Long featureId) {
		PlanFeatureMappingEntity entity = planFeatureRepository.findByPlan_IdAndFeature_IdAndDeletedFalse(planId, featureId)
				.orElseThrow(() -> new InvalidSubscriptionOperationException("Plan feature mapping not found"));
		entity.setDeleted(Boolean.TRUE);
		planFeatureRepository.save(entity);
	}

	private void validateRequest(SubscriptionFeatureEntity feature, PlanFeatureUpsertRequest request) {
		if (feature.getFeatureType() == FeatureType.BOOLEAN && request.getIsEnabled() == null) {
			throw new InvalidSubscriptionOperationException("Boolean feature requires isEnabled");
		}
		if ((feature.getFeatureType() == FeatureType.LIMIT || feature.getFeatureType() == FeatureType.QUOTA)
				&& request.getLimitValue() != null
				&& request.getLimitValue() < 0) {
			throw new InvalidSubscriptionOperationException("Limit value must be non-negative");
		}
	}

	private String trimToNull(String value) {
		return value == null || value.isBlank() ? null : value.trim();
	}
}
