package com.src.main.auth.dto.response;

import java.util.List;

public class RbacCatalogResponseDto {
	private List<RoleResponseDto> roles;
	private List<PermissionResponseDto> permissions;

	public RbacCatalogResponseDto() {
	}

	public RbacCatalogResponseDto(List<RoleResponseDto> roles, List<PermissionResponseDto> permissions) {
		this.roles = roles;
		this.permissions = permissions;
	}

	public List<RoleResponseDto> getRoles() {
		return roles;
	}

	public void setRoles(List<RoleResponseDto> roles) {
		this.roles = roles;
	}

	public List<PermissionResponseDto> getPermissions() {
		return permissions;
	}

	public void setPermissions(List<PermissionResponseDto> permissions) {
		this.permissions = permissions;
	}
}
