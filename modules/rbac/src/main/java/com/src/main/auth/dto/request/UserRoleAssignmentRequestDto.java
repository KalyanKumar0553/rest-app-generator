package com.src.main.auth.dto.request;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;

public class UserRoleAssignmentRequestDto {
	@NotEmpty
	private List<String> roleNames;

	public List<String> getRoleNames() {
		return roleNames;
	}

	public void setRoleNames(List<String> roleNames) {
		this.roleNames = roleNames;
	}
}
