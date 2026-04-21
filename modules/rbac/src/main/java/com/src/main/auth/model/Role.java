package com.src.main.auth.model;

import java.util.ArrayList;
import java.util.List;

import com.src.main.auth.config.RbacDbTables;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = RbacDbTables.ROLES)
public class Role {
	@Id
	@Column(name = "name", nullable = false)
	private String name;

	@Enumerated(EnumType.STRING)
	@Column(name = "type", nullable = false, length = 32)
	private RoleType type;

	@Column(name = "active", nullable = false)
	private boolean active = true;

	@Column(name = "display_name")
	private String displayName;

	@Column(name = "description")
	private String description;

	@Column(name = "system_role", nullable = false)
	private boolean systemRole;

	@OneToMany(mappedBy = "role")
	private List<UserRole> users = new ArrayList<>();

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public RoleType getType() {
		return type;
	}

	public void setType(RoleType type) {
		this.type = type;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
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

	public boolean isSystemRole() {
		return systemRole;
	}

	public void setSystemRole(boolean systemRole) {
		this.systemRole = systemRole;
	}
}
