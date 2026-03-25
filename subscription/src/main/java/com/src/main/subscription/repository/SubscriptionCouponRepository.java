package com.src.main.subscription.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.src.main.subscription.entity.SubscriptionCouponEntity;

public interface SubscriptionCouponRepository extends JpaRepository<SubscriptionCouponEntity, Long> {
	Optional<SubscriptionCouponEntity> findByCodeAndDeletedFalse(String code);
	boolean existsByCodeIgnoreCaseAndDeletedFalse(String code);
	List<SubscriptionCouponEntity> findAllByDeletedFalseOrderByCreatedAtDesc();
	long countByIdAndDeletedFalseAndIsActiveTrueAndValidFromLessThanEqualAndValidToIsNull(Long couponId, LocalDateTime asOf);
	long countByIdAndDeletedFalseAndIsActiveTrueAndValidFromLessThanEqualAndValidToGreaterThanEqual(Long couponId, LocalDateTime from, LocalDateTime to);
}
