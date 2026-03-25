package com.src.main.subscription.service.impl;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.src.main.auth.model.Role;
import com.src.main.auth.repository.RoleRepository;
import com.src.main.subscription.dto.PlanRequest;
import com.src.main.subscription.dto.PlanResponse;
import com.src.main.subscription.entity.SubscriptionPlanEntity;
import com.src.main.subscription.entity.SubscriptionPlanRoleMappingEntity;
import com.src.main.subscription.exception.InvalidSubscriptionOperationException;
import com.src.main.subscription.exception.PlanNotFoundException;
import com.src.main.subscription.repository.SubscriptionPlanRepository;
import com.src.main.subscription.repository.SubscriptionPlanRoleMappingRepository;
import com.src.main.subscription.service.SubscriptionPlanService;
import com.src.main.subscription.util.SubscriptionMapperUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SubscriptionPlanServiceImpl implements SubscriptionPlanService {

	private final SubscriptionPlanRepository planRepository;
	private final RoleRepository roleRepository;
	private final SubscriptionPlanRoleMappingRepository planRoleMappingRepository;
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
		SubscriptionPlanEntity saved = planRepository.save(entity);
		replacePlanRoles(saved, request.getRoleNames());
		return toPlanResponse(saved);
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
		SubscriptionPlanEntity saved = planRepository.save(entity);
		replacePlanRoles(saved, request.getRoleNames());
		return toPlanResponse(saved);
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
		return toPlanResponse(getEntity(id));
	}

	@Override
	@Transactional(readOnly = true)
	public List<PlanResponse> getAllPlans(Boolean activeOnly) {
		List<SubscriptionPlanEntity> entities = Boolean.TRUE.equals(activeOnly)
				? planRepository.findAllByIsActiveTrueAndDeletedFalseOrderBySortOrderAscNameAsc()
				: planRepository.findAllByDeletedFalseOrderBySortOrderAscNameAsc();
		return entities.stream().map(this::toPlanResponse).toList();
	}

	@Override
	@Transactional(readOnly = true)
	public List<String> getPlanRoleNames(Long planId) {
		return planRoleMappingRepository.findAllByPlan_IdAndDeletedFalse(planId).stream()
				.map(SubscriptionPlanRoleMappingEntity::getRoleName)
				.sorted()
				.toList();
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

	private PlanResponse toPlanResponse(SubscriptionPlanEntity entity) {
		return SubscriptionMapperUtil.toPlanResponse(entity, getPlanRoleNames(entity.getId()));
	}

	private void replacePlanRoles(SubscriptionPlanEntity plan, List<String> roleNames) {
		planRoleMappingRepository.deleteByPlan_Id(plan.getId());
		Set<String> normalizedRoleNames = normalizeRoleNames(roleNames);
		if (normalizedRoleNames.isEmpty()) {
			return;
		}
		List<Role> roles = roleRepository.findByNameIn(normalizedRoleNames);
		if (roles.size() != normalizedRoleNames.size()) {
			Set<String> found = roles.stream().map(Role::getName).collect(Collectors.toSet());
			List<String> missing = normalizedRoleNames.stream().filter(roleName -> !found.contains(roleName)).sorted().toList();
			throw new InvalidSubscriptionOperationException("Unknown roles for plan: " + String.join(", ", missing));
		}
		List<SubscriptionPlanRoleMappingEntity> mappings = normalizedRoleNames.stream().map(roleName -> {
			SubscriptionPlanRoleMappingEntity mapping = new SubscriptionPlanRoleMappingEntity();
			mapping.setPlan(plan);
			mapping.setRoleName(roleName);
			return mapping;
		}).toList();
		planRoleMappingRepository.saveAll(mappings);
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

	private Set<String> normalizeRoleNames(List<String> roleNames) {
		if (roleNames == null) {
			return Set.of();
		}
		return roleNames.stream()
				.filter(value -> value != null && !value.isBlank())
				.map(value -> value.trim().toUpperCase(Locale.ROOT).replace('-', '_').replace(' ', '_'))
				.map(value -> value.startsWith("ROLE_") ? value : "ROLE_" + value)
				.collect(Collectors.toCollection(java.util.LinkedHashSet::new));
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
