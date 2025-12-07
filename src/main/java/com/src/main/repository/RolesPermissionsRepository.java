package com.src.main.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.src.main.model.RolePermissions;

public interface RolesPermissionsRepository extends JpaRepository<RolePermissions, Long> {
	List<RolePermissions> findAllByRoleIn(List<String> role);
}
