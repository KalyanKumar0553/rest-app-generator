package com.src.main.subscription.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.src.main.auth.service.AccessProfileRoleProvider;
import com.src.main.subscription.entity.CustomerSubscriptionEntity;
import com.src.main.subscription.entity.SubscriptionPlanRoleMappingEntity;
import com.src.main.subscription.entity.SubscriptionRoleAssignmentEntity;
import com.src.main.subscription.enums.SubscriptionStatus;
import com.src.main.subscription.repository.SubscriptionPlanRoleMappingRepository;
import com.src.main.subscription.repository.SubscriptionRoleAssignmentRepository;
import com.src.main.subscription.service.SubscriptionRoleAssignmentService;

@Service
public class SubscriptionRoleAssignmentServiceImpl implements SubscriptionRoleAssignmentService, AccessProfileRoleProvider {
	private final SubscriptionPlanRoleMappingRepository planRoleMappingRepository;
	private final SubscriptionRoleAssignmentRepository roleAssignmentRepository;

	@Override
	@Transactional
	@CacheEvict(cacheNames = "rbacAccessProfile", key = "#subscription.subscriberUserId", condition = "#subscription != null && #subscription.subscriberUserId != null")
	public void syncAssignments(CustomerSubscriptionEntity subscription) {
		if (subscription == null || subscription.getSubscriberUserId() == null || subscription.getSubscriberUserId().isBlank()) {
			return;
		}
		roleAssignmentRepository.deactivateActiveAssignments(subscription.getTenantId(), subscription.getSubscriberUserId());
		if (!isRoleGrantingStatus(subscription.getStatus())) {
			return;
		}
		List<String> roleNames = planRoleMappingRepository.findAllByPlan_IdAndDeletedFalse(subscription.getPlan().getId()).stream().map(SubscriptionPlanRoleMappingEntity::getRoleName).distinct().toList();
		if (roleNames.isEmpty()) {
			return;
		}
		LocalDateTime now = LocalDateTime.now();
		List<SubscriptionRoleAssignmentEntity> assignments = roleNames.stream().map(roleName -> {
			SubscriptionRoleAssignmentEntity assignment = new SubscriptionRoleAssignmentEntity();
			assignment.setTenantId(subscription.getTenantId());
			assignment.setUserId(subscription.getSubscriberUserId());
			assignment.setSubscription(subscription);
			assignment.setRoleName(roleName);
			assignment.setIsActive(Boolean.TRUE);
			assignment.setAssignedAt(now);
			return assignment;
		}).toList();
		roleAssignmentRepository.saveAll(assignments);
	}

	@Override
	@Transactional(readOnly = true)
	public List<String> getActiveRolesForUser(String userId) {
		return roleAssignmentRepository.findDistinctActiveRoleNamesByUserId(userId);
	}

	@Override
	@Transactional(readOnly = true)
	public List<String> getAdditionalRoles(String userId) {
		return getActiveRolesForUser(userId);
	}

	private boolean isRoleGrantingStatus(SubscriptionStatus status) {
		return status == SubscriptionStatus.ACTIVE || status == SubscriptionStatus.TRIAL || status == SubscriptionStatus.PAYMENT_PENDING;
	}

	public SubscriptionRoleAssignmentServiceImpl(final SubscriptionPlanRoleMappingRepository planRoleMappingRepository, final SubscriptionRoleAssignmentRepository roleAssignmentRepository) {
		this.planRoleMappingRepository = planRoleMappingRepository;
		this.roleAssignmentRepository = roleAssignmentRepository;
	}
}
