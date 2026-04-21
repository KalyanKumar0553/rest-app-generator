package com.src.main.subscription.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.src.main.subscription.entity.CustomerFeatureOverrideEntity;

public interface CustomerFeatureOverrideRepository extends JpaRepository<CustomerFeatureOverrideEntity, Long> {

	@Query("""
			select cfo from CustomerFeatureOverrideEntity cfo
			where cfo.deleted = false
			  and cfo.isActive = true
			  and cfo.tenantId = :tenantId
			  and cfo.effectiveFrom <= :now
			  and (cfo.effectiveTo is null or cfo.effectiveTo >= :now)
			""")
	List<CustomerFeatureOverrideEntity> findAllActiveOverrides(Long tenantId, LocalDateTime now);

	List<CustomerFeatureOverrideEntity> findAllByTenantIdAndDeletedFalseOrderByEffectiveFromDesc(Long tenantId);
}
