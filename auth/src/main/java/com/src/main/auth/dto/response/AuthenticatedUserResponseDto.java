package com.src.main.auth.dto.response;

import java.util.List;

public class AuthenticatedUserResponseDto {
	private String id;
	private String email;
	private String name;
	private String role;
	private List<String> roles;
	private List<String> permissions;
	private String avatarUrl;

	public AuthenticatedUserResponseDto() {}

	public AuthenticatedUserResponseDto(String id, String email, String name, String role, List<String> roles, List<String> permissions, String avatarUrl) {
		this.id = id;
		this.email = email;
		this.name = name;
		this.role = role;
		this.roles = roles;
		this.permissions = permissions;
		this.avatarUrl = avatarUrl;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
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

	public String getAvatarUrl() {
		return avatarUrl;
	}

	public void setAvatarUrl(String avatarUrl) {
		this.avatarUrl = avatarUrl;
	}
}
