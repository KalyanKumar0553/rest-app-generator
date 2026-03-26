package com.src.main.dto;

import jakarta.validation.constraints.NotBlank;

public class ProjectContributorUpsertRequestDTO {
	@NotBlank(message = "userId is required")
	private String userId;

	public ProjectContributorUpsertRequestDTO() {
	}

	public String getUserId() {
		return this.userId;
	}

	public void setUserId(final String userId) {
		this.userId = userId;
	}

	@Override
	public boolean equals(final Object o) {
		if (o == this) return true;
		if (!(o instanceof ProjectContributorUpsertRequestDTO)) return false;
		final ProjectContributorUpsertRequestDTO other = (ProjectContributorUpsertRequestDTO) o;
		if (!other.canEqual((Object) this)) return false;
		final Object this$userId = this.getUserId();
		final Object other$userId = other.getUserId();
		if (this$userId == null ? other$userId != null : !this$userId.equals(other$userId)) return false;
		return true;
	}

	protected boolean canEqual(final Object other) {
		return other instanceof ProjectContributorUpsertRequestDTO;
	}

	@Override
	public int hashCode() {
		final int PRIME = 59;
		int result = 1;
		final Object $userId = this.getUserId();
		result = result * PRIME + ($userId == null ? 43 : $userId.hashCode());
		return result;
	}

	@Override
	public String toString() {
		return "ProjectContributorUpsertRequestDTO(userId=" + this.getUserId() + ")";
	}
}
