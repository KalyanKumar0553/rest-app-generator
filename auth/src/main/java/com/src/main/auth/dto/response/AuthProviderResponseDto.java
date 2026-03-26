package com.src.main.auth.dto.response;

public class AuthProviderResponseDto {
	private boolean googleEnabled;
	private boolean keycloakEnabled;

	public boolean isGoogleEnabled() {
		return this.googleEnabled;
	}

	public boolean isKeycloakEnabled() {
		return this.keycloakEnabled;
	}

	public void setGoogleEnabled(final boolean googleEnabled) {
		this.googleEnabled = googleEnabled;
	}

	public void setKeycloakEnabled(final boolean keycloakEnabled) {
		this.keycloakEnabled = keycloakEnabled;
	}

	@Override
	public boolean equals(final Object o) {
		if (o == this) return true;
		if (!(o instanceof AuthProviderResponseDto)) return false;
		final AuthProviderResponseDto other = (AuthProviderResponseDto) o;
		if (!other.canEqual((Object) this)) return false;
		if (this.isGoogleEnabled() != other.isGoogleEnabled()) return false;
		if (this.isKeycloakEnabled() != other.isKeycloakEnabled()) return false;
		return true;
	}

	protected boolean canEqual(final Object other) {
		return other instanceof AuthProviderResponseDto;
	}

	@Override
	public int hashCode() {
		final int PRIME = 59;
		int result = 1;
		result = result * PRIME + (this.isGoogleEnabled() ? 79 : 97);
		result = result * PRIME + (this.isKeycloakEnabled() ? 79 : 97);
		return result;
	}

	@Override
	public String toString() {
		return "AuthProviderResponseDto(googleEnabled=" + this.isGoogleEnabled() + ", keycloakEnabled=" + this.isKeycloakEnabled() + ")";
	}

	public AuthProviderResponseDto() {
	}

	public AuthProviderResponseDto(final boolean googleEnabled, final boolean keycloakEnabled) {
		this.googleEnabled = googleEnabled;
		this.keycloakEnabled = keycloakEnabled;
	}
}
