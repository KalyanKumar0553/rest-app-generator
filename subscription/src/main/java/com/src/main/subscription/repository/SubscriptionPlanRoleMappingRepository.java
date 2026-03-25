package com.src.main.subscription.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import com.src.main.subscription.entity.SubscriptionPlanRoleMappingEntity;

public interface SubscriptionPlanRoleMappingRepository extends JpaRepository<SubscriptionPlanRoleMappingEntity, Long> {
	List<SubscriptionPlanRoleMappingEntity> findAllByPlan_IdAndDeletedFalse(Long planId);
	List<SubscriptionPlanRoleMappingEntity> findAllByRoleNameInAndDeletedFalse(List<String> roleNames);
	@Modifying
	void deleteByPlan_Id(Long planId);
}
