package com.src.main.dto;

import jakarta.validation.constraints.Size;

public class ArtifactVersionCreateRequestDTO {
	@Size(max = 64, message = "Version code can contain up to 64 characters")
	private String versionCode;

	public ArtifactVersionCreateRequestDTO() {
	}

	public String getVersionCode() {
		return this.versionCode;
	}

	public void setVersionCode(final String versionCode) {
		this.versionCode = versionCode;
	}

	@Override
	public boolean equals(final Object o) {
		if (o == this) return true;
		if (!(o instanceof ArtifactVersionCreateRequestDTO)) return false;
		final ArtifactVersionCreateRequestDTO other = (ArtifactVersionCreateRequestDTO) o;
		if (!other.canEqual((Object) this)) return false;
		final Object this$versionCode = this.getVersionCode();
		final Object other$versionCode = other.getVersionCode();
		if (this$versionCode == null ? other$versionCode != null : !this$versionCode.equals(other$versionCode)) return false;
		return true;
	}

	protected boolean canEqual(final Object other) {
		return other instanceof ArtifactVersionCreateRequestDTO;
	}

	@Override
	public int hashCode() {
		final int PRIME = 59;
		int result = 1;
		final Object $versionCode = this.getVersionCode();
		result = result * PRIME + ($versionCode == null ? 43 : $versionCode.hashCode());
		return result;
	}

	@Override
	public String toString() {
		return "ArtifactVersionCreateRequestDTO(versionCode=" + this.getVersionCode() + ")";
	}
}
