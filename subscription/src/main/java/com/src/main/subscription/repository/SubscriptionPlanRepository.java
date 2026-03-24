package com.src.main.subscription.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.src.main.subscription.entity.SubscriptionPlanEntity;

public interface SubscriptionPlanRepository extends JpaRepository<SubscriptionPlanEntity, Long> {
	Optional<SubscriptionPlanEntity> findByCodeAndDeletedFalse(String code);

	Optional<SubscriptionPlanEntity> findByIsDefaultTrueAndIsActiveTrueAndDeletedFalse();

	List<SubscriptionPlanEntity> findAllByIsActiveTrueAndDeletedFalseOrderBySortOrderAscNameAsc();

	List<SubscriptionPlanEntity> findAllByDeletedFalseOrderBySortOrderAscNameAsc();

	boolean existsByCodeIgnoreCaseAndDeletedFalse(String code);
}
