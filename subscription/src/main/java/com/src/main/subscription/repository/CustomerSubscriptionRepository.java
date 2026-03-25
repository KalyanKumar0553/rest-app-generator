package com.src.main.subscription.repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import com.src.main.subscription.entity.CustomerSubscriptionEntity;
import com.src.main.subscription.enums.SubscriptionStatus;

import jakarta.persistence.LockModeType;

public interface CustomerSubscriptionRepository extends JpaRepository<CustomerSubscriptionEntity, Long> {
	Optional<CustomerSubscriptionEntity> findTopByTenantIdAndStatusInAndDeletedFalseOrderByCreatedAtDesc(
			Long tenantId,
			Collection<SubscriptionStatus> statuses);

	List<CustomerSubscriptionEntity> findAllByTenantIdAndDeletedFalseOrderByCreatedAtDesc(Long tenantId);

	long countByTenantIdAndDeletedFalse(Long tenantId);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	Optional<CustomerSubscriptionEntity> findWithLockingById(Long id);

	@Query("""
			select cs from CustomerSubscriptionEntity cs
			where cs.deleted = false
			  and cs.scheduledChangeAt is not null
			  and cs.scheduledChangeAt <= :now
			  and cs.status in (
			  	com.src.main.subscription.enums.SubscriptionStatus.ACTIVE,
			  	com.src.main.subscription.enums.SubscriptionStatus.TRIAL
			  )
			  and cs.scheduledTargetPlan is not null
			""")
	List<CustomerSubscriptionEntity> findAllDueScheduledDowngrades(LocalDateTime now);

	@Query("""
			select cs from CustomerSubscriptionEntity cs
			where cs.deleted = false
			  and cs.endAt is not null
			  and cs.endAt <= :now
			  and cs.status in :statuses
			""")
	List<CustomerSubscriptionEntity> findAllExpired(LocalDateTime now, Collection<SubscriptionStatus> statuses);
}
