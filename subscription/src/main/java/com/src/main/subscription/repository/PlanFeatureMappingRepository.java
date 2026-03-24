package com.src.main.subscription.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.src.main.subscription.entity.PlanFeatureMappingEntity;

public interface PlanFeatureMappingRepository extends JpaRepository<PlanFeatureMappingEntity, Long> {
	List<PlanFeatureMappingEntity> findAllByPlan_IdAndDeletedFalse(Long planId);

	Optional<PlanFeatureMappingEntity> findByPlan_IdAndFeature_IdAndDeletedFalse(Long planId, Long featureId);
}
