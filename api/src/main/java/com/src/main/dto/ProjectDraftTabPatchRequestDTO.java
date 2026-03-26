package com.src.main.dto;

import java.util.Map;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ProjectDraftTabPatchRequestDTO {
	@NotBlank
	private String tabKey;
	@NotNull
	private Map<String, Object> tabData;
	@NotNull
	private Integer draftVersion;

	public ProjectDraftTabPatchRequestDTO() {
	}

	public String getTabKey() {
		return this.tabKey;
	}

	public Map<String, Object> getTabData() {
		return this.tabData;
	}

	public Integer getDraftVersion() {
		return this.draftVersion;
	}

	public void setTabKey(final String tabKey) {
		this.tabKey = tabKey;
	}

	public void setTabData(final Map<String, Object> tabData) {
		this.tabData = tabData;
	}

	public void setDraftVersion(final Integer draftVersion) {
		this.draftVersion = draftVersion;
	}

	@Override
	public boolean equals(final Object o) {
		if (o == this) return true;
		if (!(o instanceof ProjectDraftTabPatchRequestDTO)) return false;
		final ProjectDraftTabPatchRequestDTO other = (ProjectDraftTabPatchRequestDTO) o;
		if (!other.canEqual((Object) this)) return false;
		final Object this$draftVersion = this.getDraftVersion();
		final Object other$draftVersion = other.getDraftVersion();
		if (this$draftVersion == null ? other$draftVersion != null : !this$draftVersion.equals(other$draftVersion)) return false;
		final Object this$tabKey = this.getTabKey();
		final Object other$tabKey = other.getTabKey();
		if (this$tabKey == null ? other$tabKey != null : !this$tabKey.equals(other$tabKey)) return false;
		final Object this$tabData = this.getTabData();
		final Object other$tabData = other.getTabData();
		if (this$tabData == null ? other$tabData != null : !this$tabData.equals(other$tabData)) return false;
		return true;
	}

	protected boolean canEqual(final Object other) {
		return other instanceof ProjectDraftTabPatchRequestDTO;
	}

	@Override
	public int hashCode() {
		final int PRIME = 59;
		int result = 1;
		final Object $draftVersion = this.getDraftVersion();
		result = result * PRIME + ($draftVersion == null ? 43 : $draftVersion.hashCode());
		final Object $tabKey = this.getTabKey();
		result = result * PRIME + ($tabKey == null ? 43 : $tabKey.hashCode());
		final Object $tabData = this.getTabData();
		result = result * PRIME + ($tabData == null ? 43 : $tabData.hashCode());
		return result;
	}

	@Override
	public String toString() {
		return "ProjectDraftTabPatchRequestDTO(tabKey=" + this.getTabKey() + ", tabData=" + this.getTabData() + ", draftVersion=" + this.getDraftVersion() + ")";
	}
}
