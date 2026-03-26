package com.src.main.subscription.service.impl;

import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.src.main.subscription.entity.CustomerSubscriptionEntity;
import com.src.main.subscription.entity.SubscriptionFeatureEntity;
import com.src.main.subscription.entity.SubscriptionPlanEntity;
import com.src.main.subscription.enums.SubscriptionStatus;
import com.src.main.subscription.exception.FeatureNotFoundException;
import com.src.main.subscription.exception.PlanNotFoundException;
import com.src.main.subscription.repository.CustomerSubscriptionRepository;
import com.src.main.subscription.repository.SubscriptionFeatureRepository;
import com.src.main.subscription.repository.SubscriptionPlanRepository;

@Service
public class SubscriptionLookupService {
	private static final Set<SubscriptionStatus> ACTIVE_STATUSES = EnumSet.of(SubscriptionStatus.ACTIVE, SubscriptionStatus.TRIAL, SubscriptionStatus.PAYMENT_PENDING);
	private final SubscriptionPlanRepository planRepository;
	private final SubscriptionFeatureRepository featureRepository;
	private final CustomerSubscriptionRepository customerSubscriptionRepository;

	@Transactional(readOnly = true)
	@Cacheable(cacheNames = "subscriptionPlanByCode", key = "#code.trim().toUpperCase()", sync = true)
	public SubscriptionPlanEntity getPlanByCode(String code) {
		return planRepository.findByCodeAndDeletedFalse(normalizeCode(code)).orElseThrow(() -> new PlanNotFoundException(code));
	}

	@Transactional(readOnly = true)
	public SubscriptionPlanEntity getPlanById(Long planId) {
		return planRepository.findById(planId).filter(plan -> Boolean.FALSE.equals(plan.getDeleted())).orElseThrow(() -> new PlanNotFoundException(String.valueOf(planId)));
	}

	@Transactional(readOnly = true)
	public SubscriptionPlanEntity getDefaultPlan() {
		return planRepository.findByIsDefaultTrueAndIsActiveTrueAndDeletedFalse().orElseThrow(() -> new PlanNotFoundException("DEFAULT"));
	}

	@Transactional(readOnly = true)
	@Cacheable(cacheNames = "subscriptionFeatureByCode", key = "#code.trim().toUpperCase()", sync = true)
	public SubscriptionFeatureEntity getFeatureByCode(String code) {
		return featureRepository.findByCodeAndDeletedFalse(normalizeCode(code)).orElseThrow(() -> new FeatureNotFoundException(code));
	}

	@Transactional(readOnly = true)
	public SubscriptionFeatureEntity getFeatureById(Long featureId) {
		return featureRepository.findById(featureId).filter(feature -> Boolean.FALSE.equals(feature.getDeleted())).orElseThrow(() -> new FeatureNotFoundException(String.valueOf(featureId)));
	}

	@Transactional(readOnly = true)
	@Cacheable(cacheNames = "activeSubscriptionByTenant", key = "#tenantId", sync = true, unless = "#result == null")
	public CustomerSubscriptionEntity findActiveSubscription(Long tenantId) {
		Optional<CustomerSubscriptionEntity> entity = customerSubscriptionRepository.findTopByTenantIdAndStatusInAndDeletedFalseOrderByCreatedAtDesc(tenantId, ACTIVE_STATUSES);
		return entity.orElse(null);
	}

	@CacheEvict(cacheNames = "subscriptionPlanByCode", key = "#code.trim().toUpperCase()")
	public void evictPlanByCode(String code) {
	}

	@CacheEvict(cacheNames = "subscriptionPlanByCode", allEntries = true)
	public void evictAllPlanCaches() {
	}

	@CacheEvict(cacheNames = "subscriptionFeatureByCode", key = "#code.trim().toUpperCase()")
	public void evictFeatureByCode(String code) {
	}

	@CacheEvict(cacheNames = "subscriptionFeatureByCode", allEntries = true)
	public void evictAllFeatureCaches() {
	}

	@CacheEvict(cacheNames = "activeSubscriptionByTenant", key = "#tenantId")
	public void evictActiveSubscription(Long tenantId) {
	}

	private String normalizeCode(String code) {
		if (code == null) {
			return null;
		}
		return code.trim().toUpperCase();
	}

	public SubscriptionLookupService(final SubscriptionPlanRepository planRepository, final SubscriptionFeatureRepository featureRepository, final CustomerSubscriptionRepository customerSubscriptionRepository) {
		this.planRepository = planRepository;
		this.featureRepository = featureRepository;
		this.customerSubscriptionRepository = customerSubscriptionRepository;
	}
}
