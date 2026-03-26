package com.src.main.dto;

public class ProjectDraftResponseDTO {
	private String projectId;
	private Integer draftVersion;

	public String getProjectId() {
		return this.projectId;
	}

	public Integer getDraftVersion() {
		return this.draftVersion;
	}

	public void setProjectId(final String projectId) {
		this.projectId = projectId;
	}

	public void setDraftVersion(final Integer draftVersion) {
		this.draftVersion = draftVersion;
	}

	@Override
	public boolean equals(final Object o) {
		if (o == this) return true;
		if (!(o instanceof ProjectDraftResponseDTO)) return false;
		final ProjectDraftResponseDTO other = (ProjectDraftResponseDTO) o;
		if (!other.canEqual((Object) this)) return false;
		final Object this$draftVersion = this.getDraftVersion();
		final Object other$draftVersion = other.getDraftVersion();
		if (this$draftVersion == null ? other$draftVersion != null : !this$draftVersion.equals(other$draftVersion)) return false;
		final Object this$projectId = this.getProjectId();
		final Object other$projectId = other.getProjectId();
		if (this$projectId == null ? other$projectId != null : !this$projectId.equals(other$projectId)) return false;
		return true;
	}

	protected boolean canEqual(final Object other) {
		return other instanceof ProjectDraftResponseDTO;
	}

	@Override
	public int hashCode() {
		final int PRIME = 59;
		int result = 1;
		final Object $draftVersion = this.getDraftVersion();
		result = result * PRIME + ($draftVersion == null ? 43 : $draftVersion.hashCode());
		final Object $projectId = this.getProjectId();
		result = result * PRIME + ($projectId == null ? 43 : $projectId.hashCode());
		return result;
	}

	@Override
	public String toString() {
		return "ProjectDraftResponseDTO(projectId=" + this.getProjectId() + ", draftVersion=" + this.getDraftVersion() + ")";
	}

	public ProjectDraftResponseDTO() {
	}

	public ProjectDraftResponseDTO(final String projectId, final Integer draftVersion) {
		this.projectId = projectId;
		this.draftVersion = draftVersion;
	}
}
