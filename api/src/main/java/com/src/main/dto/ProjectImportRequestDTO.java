package com.src.main.dto;

import jakarta.validation.constraints.NotBlank;

public class ProjectImportRequestDTO {
	@NotBlank(message = "projectUrl is required")
	private String projectUrl;

	public ProjectImportRequestDTO() {
	}

	public String getProjectUrl() {
		return this.projectUrl;
	}

	public void setProjectUrl(final String projectUrl) {
		this.projectUrl = projectUrl;
	}

	@Override
	public boolean equals(final Object o) {
		if (o == this) return true;
		if (!(o instanceof ProjectImportRequestDTO)) return false;
		final ProjectImportRequestDTO other = (ProjectImportRequestDTO) o;
		if (!other.canEqual((Object) this)) return false;
		final Object this$projectUrl = this.getProjectUrl();
		final Object other$projectUrl = other.getProjectUrl();
		if (this$projectUrl == null ? other$projectUrl != null : !this$projectUrl.equals(other$projectUrl)) return false;
		return true;
	}

	protected boolean canEqual(final Object other) {
		return other instanceof ProjectImportRequestDTO;
	}

	@Override
	public int hashCode() {
		final int PRIME = 59;
		int result = 1;
		final Object $projectUrl = this.getProjectUrl();
		result = result * PRIME + ($projectUrl == null ? 43 : $projectUrl.hashCode());
		return result;
	}

	@Override
	public String toString() {
		return "ProjectImportRequestDTO(projectUrl=" + this.getProjectUrl() + ")";
	}
}
