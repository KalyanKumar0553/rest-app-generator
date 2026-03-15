package com.src.main.auth.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.src.main.auth.model.Role;
import com.src.main.auth.model.RoleType;

public interface RoleRepository extends JpaRepository<Role, String> {
	Optional<Role> findByNameAndTypeAndActiveTrue(String name, RoleType type);
}
