package com.src.main.dto;

import java.util.Map;
import jakarta.validation.constraints.NotNull;

public class ProjectDraftUpsertRequestDTO {
	@NotNull
	private Map<String, Object> draftData;
	private Integer draftVersion;

	public ProjectDraftUpsertRequestDTO() {
	}

	public Map<String, Object> getDraftData() {
		return this.draftData;
	}

	public Integer getDraftVersion() {
		return this.draftVersion;
	}

	public void setDraftData(final Map<String, Object> draftData) {
		this.draftData = draftData;
	}

	public void setDraftVersion(final Integer draftVersion) {
		this.draftVersion = draftVersion;
	}

	@Override
	public boolean equals(final Object o) {
		if (o == this) return true;
		if (!(o instanceof ProjectDraftUpsertRequestDTO)) return false;
		final ProjectDraftUpsertRequestDTO other = (ProjectDraftUpsertRequestDTO) o;
		if (!other.canEqual((Object) this)) return false;
		final Object this$draftVersion = this.getDraftVersion();
		final Object other$draftVersion = other.getDraftVersion();
		if (this$draftVersion == null ? other$draftVersion != null : !this$draftVersion.equals(other$draftVersion)) return false;
		final Object this$draftData = this.getDraftData();
		final Object other$draftData = other.getDraftData();
		if (this$draftData == null ? other$draftData != null : !this$draftData.equals(other$draftData)) return false;
		return true;
	}

	protected boolean canEqual(final Object other) {
		return other instanceof ProjectDraftUpsertRequestDTO;
	}

	@Override
	public int hashCode() {
		final int PRIME = 59;
		int result = 1;
		final Object $draftVersion = this.getDraftVersion();
		result = result * PRIME + ($draftVersion == null ? 43 : $draftVersion.hashCode());
		final Object $draftData = this.getDraftData();
		result = result * PRIME + ($draftData == null ? 43 : $draftData.hashCode());
		return result;
	}

	@Override
	public String toString() {
		return "ProjectDraftUpsertRequestDTO(draftData=" + this.getDraftData() + ", draftVersion=" + this.getDraftVersion() + ")";
	}
}
