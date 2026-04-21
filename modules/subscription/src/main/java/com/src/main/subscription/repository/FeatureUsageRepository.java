package com.src.main.subscription.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import com.src.main.subscription.entity.FeatureUsageEntity;

import jakarta.persistence.LockModeType;

public interface FeatureUsageRepository extends JpaRepository<FeatureUsageEntity, Long> {
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	Optional<FeatureUsageEntity> findByTenantIdAndFeature_IdAndPeriodKey(Long tenantId, Long featureId, String periodKey);
}
