package com.src.main.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.src.main.model.UserRoles;

public interface UserRolesRepository extends JpaRepository<UserRoles, Long> {
	Optional<UserRoles> findByUserUUID(String userID);
}
