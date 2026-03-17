package com.src.main.auth.dto.response;

import java.util.List;

public class RoleResponseDto {
	private String name;
	private String displayName;
	private String description;
	private String type;
	private boolean active;
	private boolean systemRole;
	private List<String> permissions;

	public RoleResponseDto() {
	}

	public RoleResponseDto(String name, String displayName, String description, String type, boolean active,
			boolean systemRole, List<String> permissions) {
		this.name = name;
		this.displayName = displayName;
		this.description = description;
		this.type = type;
		this.active = active;
		this.systemRole = systemRole;
		this.permissions = permissions;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public boolean isSystemRole() {
		return systemRole;
	}

	public void setSystemRole(boolean systemRole) {
		this.systemRole = systemRole;
	}

	public List<String> getPermissions() {
		return permissions;
	}

	public void setPermissions(List<String> permissions) {
		this.permissions = permissions;
	}
}
