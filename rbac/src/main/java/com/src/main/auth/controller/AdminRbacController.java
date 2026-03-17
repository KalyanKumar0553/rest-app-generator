package com.src.main.auth.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.src.main.auth.dto.common.ApiResponseDto;
import com.src.main.auth.dto.request.RoleUpdateRequestDto;
import com.src.main.auth.dto.request.RoleUpsertRequestDto;
import com.src.main.auth.dto.request.UserRoleAssignmentRequestDto;
import com.src.main.auth.dto.response.RbacCatalogResponseDto;
import com.src.main.auth.dto.response.RoleResponseDto;
import com.src.main.auth.service.RbacService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/admin/rbac")
public class AdminRbacController {
	private final RbacService rbacService;

	public AdminRbacController(RbacService rbacService) {
		this.rbacService = rbacService;
	}

	@GetMapping("/catalog")
	@PreAuthorize("hasAuthority('rbac.role.read')")
	public ResponseEntity<ApiResponseDto<RbacCatalogResponseDto>> getCatalog() {
		return ResponseEntity.ok(ApiResponseDto.ok("OK", rbacService.getCatalog()));
	}

	@GetMapping("/roles")
	@PreAuthorize("hasAuthority('rbac.role.read')")
	public ResponseEntity<ApiResponseDto<List<RoleResponseDto>>> getRoles() {
		return ResponseEntity.ok(ApiResponseDto.ok("OK", rbacService.getAssignableRoles()));
	}

	@PostMapping("/roles")
	@PreAuthorize("hasAuthority('rbac.role.manage')")
	public ResponseEntity<ApiResponseDto<RoleResponseDto>> createRole(@RequestBody @Valid RoleUpsertRequestDto request) {
		return ResponseEntity.ok(ApiResponseDto.ok("Role created", rbacService.createRole(request)));
	}

	@PutMapping("/roles/{roleName}")
	@PreAuthorize("hasAuthority('rbac.role.manage')")
	public ResponseEntity<ApiResponseDto<RoleResponseDto>> updateRole(
			@PathVariable String roleName,
			@RequestBody @Valid RoleUpdateRequestDto request) {
		return ResponseEntity.ok(ApiResponseDto.ok("Role updated", rbacService.updateRole(roleName, request)));
	}

	@PutMapping("/users/{userId}/roles")
	@PreAuthorize("hasAuthority('rbac.user-role.manage')")
	public ResponseEntity<ApiResponseDto<List<String>>> assignUserRoles(
			@PathVariable String userId,
			@RequestBody @Valid UserRoleAssignmentRequestDto request) {
		return ResponseEntity.ok(ApiResponseDto.ok("User roles updated", rbacService.assignRolesToUser(userId, request.getRoleNames())));
	}
}
