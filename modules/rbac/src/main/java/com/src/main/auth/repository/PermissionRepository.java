package com.src.main.auth.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.src.main.auth.model.Permission;

public interface PermissionRepository extends JpaRepository<Permission, String> {
	List<Permission> findByActiveTrueOrderByCategoryAscDisplayNameAsc();
	List<Permission> findByNameIn(Collection<String> names);
}
