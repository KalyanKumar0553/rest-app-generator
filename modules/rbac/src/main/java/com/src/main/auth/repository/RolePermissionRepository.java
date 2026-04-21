package com.src.main.auth.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.src.main.auth.model.RolePermission;
import com.src.main.auth.model.RolePermissionId;

public interface RolePermissionRepository extends JpaRepository<RolePermission, RolePermissionId> {
	List<RolePermission> findByRoleName(String roleName);

	@Query("""
			select distinct rp.permissionName
			from RolePermission rp
			join rp.role r
			join rp.permission p
			where rp.roleName in :roleNames
			  and r.active = true
			  and p.active = true
			order by rp.permissionName asc
			""")
	List<String> findActivePermissionNamesByRoleNames(@Param("roleNames") Collection<String> roleNames);

	@Modifying
	void deleteByRoleName(String roleName);
}
