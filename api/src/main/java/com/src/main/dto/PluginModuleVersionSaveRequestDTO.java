package com.src.main.dto;

public class PluginModuleVersionSaveRequestDTO {
	private String versionCode;
	private String changelog;

	public PluginModuleVersionSaveRequestDTO() {
	}

	public String getVersionCode() {
		return this.versionCode;
	}

	public String getChangelog() {
		return this.changelog;
	}

	public void setVersionCode(final String versionCode) {
		this.versionCode = versionCode;
	}

	public void setChangelog(final String changelog) {
		this.changelog = changelog;
	}

	@Override
	public boolean equals(final Object o) {
		if (o == this) return true;
		if (!(o instanceof PluginModuleVersionSaveRequestDTO)) return false;
		final PluginModuleVersionSaveRequestDTO other = (PluginModuleVersionSaveRequestDTO) o;
		if (!other.canEqual((Object) this)) return false;
		final Object this$versionCode = this.getVersionCode();
		final Object other$versionCode = other.getVersionCode();
		if (this$versionCode == null ? other$versionCode != null : !this$versionCode.equals(other$versionCode)) return false;
		final Object this$changelog = this.getChangelog();
		final Object other$changelog = other.getChangelog();
		if (this$changelog == null ? other$changelog != null : !this$changelog.equals(other$changelog)) return false;
		return true;
	}

	protected boolean canEqual(final Object other) {
		return other instanceof PluginModuleVersionSaveRequestDTO;
	}

	@Override
	public int hashCode() {
		final int PRIME = 59;
		int result = 1;
		final Object $versionCode = this.getVersionCode();
		result = result * PRIME + ($versionCode == null ? 43 : $versionCode.hashCode());
		final Object $changelog = this.getChangelog();
		result = result * PRIME + ($changelog == null ? 43 : $changelog.hashCode());
		return result;
	}

	@Override
	public String toString() {
		return "PluginModuleVersionSaveRequestDTO(versionCode=" + this.getVersionCode() + ", changelog=" + this.getChangelog() + ")";
	}
}
