package com.src.main.dto;

import java.util.Map;

public class ProjectDraftTabDataDTO {
	private String tabKey;
	private Map<String, Object> tabData;

	public String getTabKey() {
		return this.tabKey;
	}

	public Map<String, Object> getTabData() {
		return this.tabData;
	}

	public void setTabKey(final String tabKey) {
		this.tabKey = tabKey;
	}

	public void setTabData(final Map<String, Object> tabData) {
		this.tabData = tabData;
	}

	@Override
	public boolean equals(final Object o) {
		if (o == this) return true;
		if (!(o instanceof ProjectDraftTabDataDTO)) return false;
		final ProjectDraftTabDataDTO other = (ProjectDraftTabDataDTO) o;
		if (!other.canEqual((Object) this)) return false;
		final Object this$tabKey = this.getTabKey();
		final Object other$tabKey = other.getTabKey();
		if (this$tabKey == null ? other$tabKey != null : !this$tabKey.equals(other$tabKey)) return false;
		final Object this$tabData = this.getTabData();
		final Object other$tabData = other.getTabData();
		if (this$tabData == null ? other$tabData != null : !this$tabData.equals(other$tabData)) return false;
		return true;
	}

	protected boolean canEqual(final Object other) {
		return other instanceof ProjectDraftTabDataDTO;
	}

	@Override
	public int hashCode() {
		final int PRIME = 59;
		int result = 1;
		final Object $tabKey = this.getTabKey();
		result = result * PRIME + ($tabKey == null ? 43 : $tabKey.hashCode());
		final Object $tabData = this.getTabData();
		result = result * PRIME + ($tabData == null ? 43 : $tabData.hashCode());
		return result;
	}

	@Override
	public String toString() {
		return "ProjectDraftTabDataDTO(tabKey=" + this.getTabKey() + ", tabData=" + this.getTabData() + ")";
	}

	public ProjectDraftTabDataDTO() {
	}

	public ProjectDraftTabDataDTO(final String tabKey, final Map<String, Object> tabData) {
		this.tabKey = tabKey;
		this.tabData = tabData;
	}
}
