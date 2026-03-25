package com.src.main.auth.service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.src.main.auth.dto.request.RoleUpdateRequestDto;
import com.src.main.auth.dto.request.RoleUpsertRequestDto;
import com.src.main.auth.dto.response.PermissionResponseDto;
import com.src.main.auth.dto.response.RbacCatalogResponseDto;
import com.src.main.auth.dto.response.RoleResponseDto;
import com.src.main.auth.model.Permission;
import com.src.main.auth.model.Role;
import com.src.main.auth.model.RolePermission;
import com.src.main.auth.model.RoleType;
import com.src.main.auth.model.UserRole;
import com.src.main.auth.repository.PermissionRepository;
import com.src.main.auth.repository.RolePermissionRepository;
import com.src.main.auth.repository.RoleRepository;
import com.src.main.auth.repository.UserRoleRepository;

@Service
public class RbacService {
	public static final String ROLE_SUPER_ADMIN = "ROLE_SUPER_ADMIN";

	public record AccessProfile(List<String> roles, List<String> permissions, List<String> authorities) {
	}

	private final RoleRepository roleRepository;
	private final PermissionRepository permissionRepository;
	private final RolePermissionRepository rolePermissionRepository;
	private final UserRoleRepository userRoleRepository;
	private final JdbcTemplate jdbcTemplate;
	private final RoleCatalogService roleCatalogService;
	private final List<AccessProfileRoleProvider> accessProfileRoleProviders;

	public RbacService(
			RoleRepository roleRepository,
			PermissionRepository permissionRepository,
			RolePermissionRepository rolePermissionRepository,
			UserRoleRepository userRoleRepository,
			JdbcTemplate jdbcTemplate,
			RoleCatalogService roleCatalogService,
			List<AccessProfileRoleProvider> accessProfileRoleProviders) {
		this.roleRepository = roleRepository;
		this.permissionRepository = permissionRepository;
		this.rolePermissionRepository = rolePermissionRepository;
		this.userRoleRepository = userRoleRepository;
		this.jdbcTemplate = jdbcTemplate;
		this.roleCatalogService = roleCatalogService;
		this.accessProfileRoleProviders = accessProfileRoleProviders == null ? List.of() : List.copyOf(accessProfileRoleProviders);
	}

	@Transactional(readOnly = true)
	@Cacheable(cacheNames = "rbacAccessProfile", key = "#userId", sync = true)
	public AccessProfile getAccessProfile(String userId) {
		LinkedHashSet<String> resolvedRoles = new LinkedHashSet<>(roleRepository.findActiveRoleNamesByUserId(userId));
		for (AccessProfileRoleProvider provider : accessProfileRoleProviders) {
			List<String> additionalRoles = provider.getAdditionalRoles(userId);
			if (additionalRoles != null) {
				resolvedRoles.addAll(additionalRoles);
			}
		}
		if (resolvedRoles.isEmpty()) {
			resolvedRoles.add(roleCatalogService.getDefaultAuthRoleName());
		}
		List<String> roles = List.copyOf(resolvedRoles);
		List<String> permissions = rolePermissionRepository.findActivePermissionNamesByRoleNames(roles);
		LinkedHashSet<String> authorities = new LinkedHashSet<>();
		authorities.addAll(roles);
		authorities.addAll(permissions);
		return new AccessProfile(List.copyOf(roles), List.copyOf(permissions), List.copyOf(authorities));
	}

	public boolean currentUserHasPermission(String permission) {
		return hasPermission(SecurityContextHolder.getContext().getAuthentication(), permission);
	}

	public boolean hasPermission(Authentication authentication, String permission) {
		if (authentication == null || permission == null || permission.isBlank()) {
			return false;
		}
		boolean grantedInAuthentication = authentication.getAuthorities().stream()
				.anyMatch(authority -> permission.equals(authority.getAuthority()));
		if (grantedInAuthentication) {
			return true;
		}
		String userId = authentication.getName();
		if (userId == null || userId.isBlank()) {
			return false;
		}
		return getAccessProfile(userId).permissions().contains(permission);
	}

	public boolean isSuperAdmin(Authentication authentication) {
		if (authentication == null) {
			return false;
		}
		boolean grantedInAuthentication = authentication.getAuthorities().stream()
				.anyMatch(authority -> ROLE_SUPER_ADMIN.equals(authority.getAuthority()));
		if (grantedInAuthentication) {
			return true;
		}
		String userId = authentication.getName();
		if (userId == null || userId.isBlank()) {
			return false;
		}
		return getAccessProfile(userId).roles().contains(ROLE_SUPER_ADMIN);
	}

	public boolean currentUserIsSuperAdmin() {
		return isSuperAdmin(SecurityContextHolder.getContext().getAuthentication());
	}

	@Transactional(readOnly = true)
	public List<RoleResponseDto> getAssignableRoles() {
		Map<String, List<String>> permissionsByRole = rolePermissionRepository.findAll().stream()
				.collect(Collectors.groupingBy(
						RolePermission::getRoleName,
						Collectors.mapping(RolePermission::getPermissionName, Collectors.collectingAndThen(Collectors.toList(), list -> list.stream().sorted().toList()))));
		return roleRepository.findByTypeOrderByNameAsc(RoleType.AUTH_ROLE).stream()
				.map(role -> toRoleResponse(role, permissionsByRole.getOrDefault(role.getName(), List.of())))
				.toList();
	}

	@Transactional(readOnly = true)
	@Cacheable(cacheNames = "rbacCatalog", sync = true)
	public RbacCatalogResponseDto getCatalog() {
		List<RoleResponseDto> roles = getAssignableRoles();
		List<PermissionResponseDto> permissions = permissionRepository.findByActiveTrueOrderByCategoryAscDisplayNameAsc().stream()
				.map(this::toPermissionResponse)
				.toList();
		return new RbacCatalogResponseDto(roles, permissions);
	}

	@Transactional
	@Caching(evict = {
			@CacheEvict(cacheNames = "rbacCatalog", allEntries = true),
			@CacheEvict(cacheNames = "rbacAccessProfile", allEntries = true)
	})
	public RoleResponseDto createRole(RoleUpsertRequestDto request) {
		String roleName = normalizeRoleName(request.getName());
		if (roleRepository.existsById(roleName)) {
			throw new IllegalArgumentException("Role already exists: " + roleName);
		}
		Role role = new Role();
		role.setName(roleName);
		role.setType(RoleType.AUTH_ROLE);
		role.setActive(request.isActive());
		role.setDisplayName(request.getDisplayName().trim());
		role.setDescription(trimToNull(request.getDescription()));
		role.setSystemRole(false);
		roleRepository.save(role);
		replaceRolePermissions(roleName, request.getPermissions());
		return getRole(roleName);
	}

	@Transactional
	@Caching(evict = {
			@CacheEvict(cacheNames = "rbacCatalog", allEntries = true),
			@CacheEvict(cacheNames = "rbacAccessProfile", allEntries = true)
	})
	public RoleResponseDto updateRole(String roleName, RoleUpdateRequestDto request) {
		String normalizedRoleName = normalizeRoleName(roleName);
		Role role = roleRepository.findById(normalizedRoleName)
				.orElseThrow(() -> new IllegalArgumentException("Role not found: " + normalizedRoleName));
		role.setDisplayName(request.getDisplayName().trim());
		role.setDescription(trimToNull(request.getDescription()));
		if (!ROLE_SUPER_ADMIN.equals(normalizedRoleName)) {
			role.setActive(request.isActive());
		}
		roleRepository.save(role);
		replaceRolePermissions(normalizedRoleName, request.getPermissions());
		return getRole(normalizedRoleName);
	}

	@Transactional(readOnly = true)
	public RoleResponseDto getRole(String roleName) {
		String normalizedRoleName = normalizeRoleName(roleName);
		Role role = roleRepository.findById(normalizedRoleName)
				.orElseThrow(() -> new IllegalArgumentException("Role not found: " + normalizedRoleName));
		List<String> permissions = rolePermissionRepository.findByRoleName(normalizedRoleName).stream()
				.map(RolePermission::getPermissionName)
				.sorted()
				.toList();
		return toRoleResponse(role, permissions);
	}

	@Transactional
	@Caching(evict = {
			@CacheEvict(cacheNames = "rbacCatalog", allEntries = true),
			@CacheEvict(cacheNames = "rbacAccessProfile", key = "#userId")
	})
	public List<String> assignRolesToUser(String userId, List<String> roleNames) {
		if (!userExists(userId)) {
			throw new IllegalArgumentException("User not found: " + userId);
		}
		Set<String> normalizedRoleNames = roleNames == null ? Set.of() : roleNames.stream()
				.map(this::normalizeRoleName)
				.collect(Collectors.toCollection(LinkedHashSet::new));
		if (normalizedRoleNames.isEmpty()) {
			throw new IllegalArgumentException("At least one role must be assigned");
		}
		List<Role> roles = roleRepository.findByNameIn(normalizedRoleNames);
		if (roles.size() != normalizedRoleNames.size()) {
			Set<String> foundNames = roles.stream().map(Role::getName).collect(Collectors.toSet());
			List<String> missing = normalizedRoleNames.stream().filter(name -> !foundNames.contains(name)).toList();
			throw new IllegalArgumentException("Unknown roles: " + String.join(", ", missing));
		}
		userRoleRepository.deleteByUserId(userId);
		List<UserRole> assignments = new ArrayList<>();
		for (String roleName : normalizedRoleNames) {
			UserRole assignment = new UserRole();
			assignment.setUserId(userId);
			assignment.setRoleName(roleName);
			assignments.add(assignment);
		}
		userRoleRepository.saveAll(assignments);
		return getAccessProfile(userId).roles();
	}

	private void replaceRolePermissions(String roleName, List<String> permissionNames) {
		Set<String> normalizedPermissions = permissionNames == null ? Set.of() : permissionNames.stream()
				.map(String::trim)
				.filter(value -> !value.isEmpty())
				.collect(Collectors.toCollection(LinkedHashSet::new));
		if (normalizedPermissions.isEmpty()) {
			throw new IllegalArgumentException("At least one permission is required");
		}
		List<Permission> permissions = permissionRepository.findByNameIn(normalizedPermissions);
		if (permissions.size() != normalizedPermissions.size()) {
			Set<String> found = permissions.stream().map(Permission::getName).collect(Collectors.toSet());
			List<String> missing = normalizedPermissions.stream().filter(name -> !found.contains(name)).toList();
			throw new IllegalArgumentException("Unknown permissions: " + String.join(", ", missing));
		}
		rolePermissionRepository.deleteByRoleName(roleName);
		List<RolePermission> newAssignments = new ArrayList<>();
		for (String permissionName : normalizedPermissions) {
			RolePermission assignment = new RolePermission();
			assignment.setRoleName(roleName);
			assignment.setPermissionName(permissionName);
			newAssignments.add(assignment);
		}
		rolePermissionRepository.saveAll(newAssignments);
	}

	private String normalizeRoleName(String roleName) {
		if (roleName == null || roleName.isBlank()) {
			throw new IllegalArgumentException("Role name is required");
		}
		String normalized = roleName.trim().toUpperCase(Locale.ROOT).replace('-', '_').replace(' ', '_');
		return normalized.startsWith("ROLE_") ? normalized : "ROLE_" + normalized;
	}

	private RoleResponseDto toRoleResponse(Role role, List<String> permissions) {
		return new RoleResponseDto(
				role.getName(),
				role.getDisplayName(),
				role.getDescription(),
				role.getType() == null ? null : role.getType().name(),
				role.isActive(),
				role.isSystemRole(),
				permissions);
	}

	private PermissionResponseDto toPermissionResponse(Permission permission) {
		return new PermissionResponseDto(
				permission.getName(),
				permission.getDisplayName(),
				permission.getDescription(),
				permission.getCategory());
	}

	private String trimToNull(String value) {
		if (value == null) {
			return null;
		}
		String trimmed = value.trim();
		return trimmed.isEmpty() ? null : trimmed;
	}

	private boolean userExists(String userId) {
		Integer count = jdbcTemplate.queryForObject(
				"select count(*) from users where id = ?",
				Integer.class,
				userId);
		return count != null && count > 0;
	}
}
