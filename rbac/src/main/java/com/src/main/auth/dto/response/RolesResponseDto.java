package com.src.main.auth.dto.response;

import java.util.List;

public class RolesResponseDto {
	private List<String> roles;
	private List<String> permissions;

	public RolesResponseDto() {}

	public RolesResponseDto(List<String> roles) {
		this.roles = roles;
	}

	public RolesResponseDto(List<String> roles, List<String> permissions) {
		this.roles = roles;
		this.permissions = permissions;
	}

	public List<String> getRoles() {
		return roles;
	}

	public void setRoles(List<String> roles) {
		this.roles = roles;
	}

	public List<String> getPermissions() {
		return permissions;
	}

	public void setPermissions(List<String> permissions) {
		this.permissions = permissions;
	}
}
