package com.src.main.subscription.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.src.main.subscription.entity.SubscriptionFeatureEntity;

public interface SubscriptionFeatureRepository extends JpaRepository<SubscriptionFeatureEntity, Long> {
	Optional<SubscriptionFeatureEntity> findByCodeAndDeletedFalse(String code);

	List<SubscriptionFeatureEntity> findAllByIsActiveTrueAndDeletedFalseOrderByNameAsc();

	List<SubscriptionFeatureEntity> findAllByDeletedFalseOrderByNameAsc();
}
