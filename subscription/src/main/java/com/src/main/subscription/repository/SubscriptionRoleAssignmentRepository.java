package com.src.main.subscription.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.src.main.subscription.entity.SubscriptionRoleAssignmentEntity;

public interface SubscriptionRoleAssignmentRepository extends JpaRepository<SubscriptionRoleAssignmentEntity, Long> {
	@Query("""
			select distinct sra.roleName
			from SubscriptionRoleAssignmentEntity sra
			where sra.userId = :userId
			  and sra.isActive = true
			  and sra.deleted = false
			order by sra.roleName asc
			""")
	List<String> findDistinctActiveRoleNamesByUserId(@Param("userId") String userId);

	List<SubscriptionRoleAssignmentEntity> findAllByTenantIdAndUserIdAndIsActiveTrueAndDeletedFalse(Long tenantId, String userId);

	@Modifying
	@Query("""
			update SubscriptionRoleAssignmentEntity sra
			set sra.isActive = false,
			    sra.revokedAt = CURRENT_TIMESTAMP
			where sra.tenantId = :tenantId
			  and sra.userId = :userId
			  and sra.isActive = true
			  and sra.deleted = false
			""")
	void deactivateActiveAssignments(Long tenantId, String userId);
}
