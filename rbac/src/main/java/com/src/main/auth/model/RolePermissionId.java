package com.src.main.auth.model;

import java.io.Serializable;
import java.util.Objects;

public class RolePermissionId implements Serializable {
	private String roleName;
	private String permissionName;

	public RolePermissionId() {
	}

	public RolePermissionId(String roleName, String permissionName) {
		this.roleName = roleName;
		this.permissionName = permissionName;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof RolePermissionId other)) {
			return false;
		}
		return Objects.equals(roleName, other.roleName) && Objects.equals(permissionName, other.permissionName);
	}

	@Override
	public int hashCode() {
		return Objects.hash(roleName, permissionName);
	}
}
