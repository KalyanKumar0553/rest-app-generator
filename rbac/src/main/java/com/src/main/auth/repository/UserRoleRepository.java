package com.src.main.auth.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import com.src.main.auth.model.UserRole;
import com.src.main.auth.model.UserRoleId;

public interface UserRoleRepository extends JpaRepository<UserRole, UserRoleId> {
	List<UserRole> findByUserId(String userId);
	@Modifying
	void deleteByUserId(String userId);
}
