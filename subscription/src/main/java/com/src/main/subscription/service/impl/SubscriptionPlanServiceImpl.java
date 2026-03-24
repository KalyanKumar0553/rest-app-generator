package com.src.main.subscription.service.impl;

import java.util.List;
import java.util.Locale;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.src.main.subscription.dto.PlanRequest;
import com.src.main.subscription.dto.PlanResponse;
import com.src.main.subscription.entity.SubscriptionPlanEntity;
import com.src.main.subscription.exception.InvalidSubscriptionOperationException;
import com.src.main.subscription.exception.PlanNotFoundException;
import com.src.main.subscription.repository.SubscriptionPlanRepository;
import com.src.main.subscription.service.SubscriptionPlanService;
import com.src.main.subscription.util.SubscriptionMapperUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SubscriptionPlanServiceImpl implements SubscriptionPlanService {

	private final SubscriptionPlanRepository planRepository;
	private final SubscriptionLookupService lookupService;

	@Override
	@Transactional
	@Caching(evict = {
			@CacheEvict(cacheNames = "subscriptionPlanByCode", allEntries = true),
			@CacheEvict(cacheNames = "entitlementsByTenant", allEntries = true)
	})
	public PlanResponse createPlan(PlanRequest request) {
		String code = normalizeCode(request.getCode());
		if (planRepository.existsByCodeIgnoreCaseAndDeletedFalse(code)) {
			throw new InvalidSubscriptionOperationException("Plan code already exists: " + code);
		}
		SubscriptionPlanEntity entity = new SubscriptionPlanEntity();
		apply(entity, request, code);
		if (Boolean.TRUE.equals(entity.getIsDefault())) {
			clearDefaultPlan();
		}
		return SubscriptionMapperUtil.toPlanResponse(planRepository.save(entity));
	}

	@Override
	@Transactional
	@Caching(evict = {
			@CacheEvict(cacheNames = "subscriptionPlanByCode", allEntries = true),
			@CacheEvict(cacheNames = "entitlementsByTenant", allEntries = true)
	})
	public PlanResponse updatePlan(Long id, PlanRequest request) {
		SubscriptionPlanEntity entity = planRepository.findById(id)
				.filter(plan -> Boolean.FALSE.equals(plan.getDeleted()))
				.orElseThrow(() -> new PlanNotFoundException(String.valueOf(id)));
		String code = normalizeCode(request.getCode());
		if (!entity.getCode().equalsIgnoreCase(code) && planRepository.existsByCodeIgnoreCaseAndDeletedFalse(code)) {
			throw new InvalidSubscriptionOperationException("Plan code already exists: " + code);
		}
		if (Boolean.TRUE.equals(request.getIsDefault())) {
			clearDefaultPlan();
		}
		apply(entity, request, code);
		return SubscriptionMapperUtil.toPlanResponse(planRepository.save(entity));
	}

	@Override
	@Transactional
	@Caching(evict = {
			@CacheEvict(cacheNames = "subscriptionPlanByCode", allEntries = true),
			@CacheEvict(cacheNames = "entitlementsByTenant", allEntries = true)
	})
	public void activatePlan(Long id) {
		SubscriptionPlanEntity entity = getEntity(id);
		entity.setIsActive(Boolean.TRUE);
		planRepository.save(entity);
	}

	@Override
	@Transactional
	@Caching(evict = {
			@CacheEvict(cacheNames = "subscriptionPlanByCode", allEntries = true),
			@CacheEvict(cacheNames = "entitlementsByTenant", allEntries = true)
	})
	public void deactivatePlan(Long id) {
		SubscriptionPlanEntity entity = getEntity(id);
		if (Boolean.TRUE.equals(entity.getIsDefault())) {
			throw new InvalidSubscriptionOperationException("Default plan cannot be deactivated");
		}
		entity.setIsActive(Boolean.FALSE);
		planRepository.save(entity);
	}

	@Override
	@Transactional
	@Caching(evict = {
			@CacheEvict(cacheNames = "subscriptionPlanByCode", allEntries = true),
			@CacheEvict(cacheNames = "entitlementsByTenant", allEntries = true)
	})
	public void setDefaultPlan(Long id) {
		SubscriptionPlanEntity entity = getEntity(id);
		entity.setIsActive(Boolean.TRUE);
		clearDefaultPlan();
		entity.setIsDefault(Boolean.TRUE);
		planRepository.save(entity);
	}

	@Override
	@Transactional(readOnly = true)
	public PlanResponse getPlan(Long id) {
		return SubscriptionMapperUtil.toPlanResponse(getEntity(id));
	}

	@Override
	@Transactional(readOnly = true)
	public List<PlanResponse> getAllPlans(Boolean activeOnly) {
		List<SubscriptionPlanEntity> entities = Boolean.TRUE.equals(activeOnly)
				? planRepository.findAllByIsActiveTrueAndDeletedFalseOrderBySortOrderAscNameAsc()
				: planRepository.findAllByDeletedFalseOrderBySortOrderAscNameAsc();
		return entities.stream().map(SubscriptionMapperUtil::toPlanResponse).toList();
	}

	private SubscriptionPlanEntity getEntity(Long id) {
		return planRepository.findById(id)
				.filter(plan -> Boolean.FALSE.equals(plan.getDeleted()))
				.orElseThrow(() -> new PlanNotFoundException(String.valueOf(id)));
	}

	private void clearDefaultPlan() {
		planRepository.findByIsDefaultTrueAndIsActiveTrueAndDeletedFalse().ifPresent(existing -> {
			existing.setIsDefault(Boolean.FALSE);
			planRepository.save(existing);
		});
	}

	private void apply(SubscriptionPlanEntity entity, PlanRequest request, String code) {
		entity.setCode(code);
		entity.setName(requireText(request.getName(), "Plan name is required"));
		entity.setDescription(trimToNull(request.getDescription()));
		entity.setIsActive(Boolean.TRUE.equals(request.getIsActive()));
		entity.setIsDefault(Boolean.TRUE.equals(request.getIsDefault()));
		entity.setSortOrder(request.getSortOrder() == null ? 0 : request.getSortOrder());
		entity.setTrialDays(request.getTrialDays());
		entity.setPlanType(request.getPlanType());
		entity.setVisibility(request.getVisibility());
		entity.setMaxUsers(request.getMaxUsers());
		entity.setMaxProjects(request.getMaxProjects());
		entity.setMaxStorageMb(request.getMaxStorageMb());
		entity.setMetadataJson(trimToNull(request.getMetadataJson()));
		if (Boolean.TRUE.equals(entity.getIsDefault()) && !Boolean.TRUE.equals(entity.getIsActive())) {
			throw new InvalidSubscriptionOperationException("Default plan must be active");
		}
	}

	private String normalizeCode(String value) {
		return requireText(value, "Plan code is required")
				.toUpperCase(Locale.ROOT)
				.replace('-', '_')
				.replace(' ', '_');
	}

	private String requireText(String value, String message) {
		if (value == null || value.isBlank()) {
			throw new InvalidSubscriptionOperationException(message);
		}
		return value.trim();
	}

	private String trimToNull(String value) {
		return value == null || value.isBlank() ? null : value.trim();
	}
}
