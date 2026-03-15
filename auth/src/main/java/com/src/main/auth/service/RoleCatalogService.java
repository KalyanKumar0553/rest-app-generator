package com.src.main.auth.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.src.main.auth.model.Role;
import com.src.main.auth.model.RoleType;
import com.src.main.auth.repository.RoleRepository;

@Service
public class RoleCatalogService {
	private final RoleRepository roleRepository;
	private final String defaultAuthRoleName;

	public RoleCatalogService(
			RoleRepository roleRepository,
			@Value("${app.auth.default-role-name:ROLE_GUEST}") String defaultAuthRoleName) {
		this.roleRepository = roleRepository;
		this.defaultAuthRoleName = defaultAuthRoleName;
	}

	public String getDefaultAuthRoleName() {
		return requireRole(defaultAuthRoleName, RoleType.AUTH_ROLE).getName();
	}

	public Role requireRole(String roleName, RoleType roleType) {
		return roleRepository.findByNameAndTypeAndActiveTrue(roleName, roleType)
				.orElseThrow(() -> new IllegalStateException("Role not configured in roles table: " + roleName));
	}
}
