package com.src.main.subscription.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.src.main.subscription.entity.PlanPriceEntity;
import com.src.main.subscription.enums.BillingCycle;

public interface PlanPriceRepository extends JpaRepository<PlanPriceEntity, Long> {
	List<PlanPriceEntity> findAllByPlan_IdAndDeletedFalseOrderByEffectiveFromDesc(Long planId);

	List<PlanPriceEntity> findAllByPlan_CodeAndDeletedFalseOrderByEffectiveFromDesc(String planCode);

	Optional<PlanPriceEntity> findTopByPlan_CodeAndBillingCycleAndCurrencyCodeAndIsActiveTrueAndDeletedFalseAndEffectiveFromLessThanEqualAndEffectiveToIsNullOrderByEffectiveFromDesc(
			String planCode,
			BillingCycle billingCycle,
			String currencyCode,
			LocalDateTime asOf);

	Optional<PlanPriceEntity> findTopByPlan_CodeAndBillingCycleAndCurrencyCodeAndIsActiveTrueAndDeletedFalseAndEffectiveFromLessThanEqualAndEffectiveToGreaterThanEqualOrderByEffectiveFromDesc(
			String planCode,
			BillingCycle billingCycle,
			String currencyCode,
			LocalDateTime asOfFrom,
			LocalDateTime asOfTo);
}
