package com.src.main.subscription.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.src.main.subscription.entity.SubscriptionAuditLogEntity;

public interface SubscriptionAuditLogRepository extends JpaRepository<SubscriptionAuditLogEntity, Long> {
	List<SubscriptionAuditLogEntity> findAllByTenantIdOrderByCreatedAtDesc(Long tenantId);
}
