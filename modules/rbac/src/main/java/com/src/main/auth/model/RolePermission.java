package com.src.main.auth.model;

import com.src.main.auth.config.RbacDbTables;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = RbacDbTables.ROLE_PERMISSIONS)
@IdClass(RolePermissionId.class)
public class RolePermission {
	@jakarta.persistence.Id
	@Column(name = "role_name", nullable = false)
	private String roleName;

	@jakarta.persistence.Id
	@Column(name = "permission_name", nullable = false)
	private String permissionName;

	@ManyToOne
	@JoinColumn(name = "role_name", referencedColumnName = "name", insertable = false, updatable = false)
	private Role role;

	@ManyToOne
	@JoinColumn(name = "permission_name", referencedColumnName = "name", insertable = false, updatable = false)
	private Permission permission;

	public String getRoleName() {
		return roleName;
	}

	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}

	public String getPermissionName() {
		return permissionName;
	}

	public void setPermissionName(String permissionName) {
		this.permissionName = permissionName;
	}

	public Role getRole() {
		return role;
	}

	public Permission getPermission() {
		return permission;
	}
}
