package com.src.main.auth.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "auth_oauth_provider_config")
public class OAuthProviderConfig {
	@Id
	@Column(name = "provider_id", nullable = false, length = 50)
	private String providerId;
	@Column(name = "enabled", nullable = false)
	private boolean enabled;
	@Column(name = "client_id", length = 300)
	private String clientId;
	@Column(name = "client_secret", length = 1000)
	private String clientSecret;
	@Column(name = "issuer_uri", length = 500)
	private String issuerUri;
	@Column(name = "scope", length = 300)
	private String scope;

	public OAuthProviderConfig() {
	}

	public String getProviderId() {
		return this.providerId;
	}

	public boolean isEnabled() {
		return this.enabled;
	}

	public String getClientId() {
		return this.clientId;
	}

	public String getClientSecret() {
		return this.clientSecret;
	}

	public String getIssuerUri() {
		return this.issuerUri;
	}

	public String getScope() {
		return this.scope;
	}

	public void setProviderId(final String providerId) {
		this.providerId = providerId;
	}

	public void setEnabled(final boolean enabled) {
		this.enabled = enabled;
	}

	public void setClientId(final String clientId) {
		this.clientId = clientId;
	}

	public void setClientSecret(final String clientSecret) {
		this.clientSecret = clientSecret;
	}

	public void setIssuerUri(final String issuerUri) {
		this.issuerUri = issuerUri;
	}

	public void setScope(final String scope) {
		this.scope = scope;
	}

	@Override
	public boolean equals(final Object o) {
		if (o == this) return true;
		if (!(o instanceof OAuthProviderConfig)) return false;
		final OAuthProviderConfig other = (OAuthProviderConfig) o;
		if (!other.canEqual((Object) this)) return false;
		if (this.isEnabled() != other.isEnabled()) return false;
		final Object this$providerId = this.getProviderId();
		final Object other$providerId = other.getProviderId();
		if (this$providerId == null ? other$providerId != null : !this$providerId.equals(other$providerId)) return false;
		final Object this$clientId = this.getClientId();
		final Object other$clientId = other.getClientId();
		if (this$clientId == null ? other$clientId != null : !this$clientId.equals(other$clientId)) return false;
		final Object this$clientSecret = this.getClientSecret();
		final Object other$clientSecret = other.getClientSecret();
		if (this$clientSecret == null ? other$clientSecret != null : !this$clientSecret.equals(other$clientSecret)) return false;
		final Object this$issuerUri = this.getIssuerUri();
		final Object other$issuerUri = other.getIssuerUri();
		if (this$issuerUri == null ? other$issuerUri != null : !this$issuerUri.equals(other$issuerUri)) return false;
		final Object this$scope = this.getScope();
		final Object other$scope = other.getScope();
		if (this$scope == null ? other$scope != null : !this$scope.equals(other$scope)) return false;
		return true;
	}

	protected boolean canEqual(final Object other) {
		return other instanceof OAuthProviderConfig;
	}

	@Override
	public int hashCode() {
		final int PRIME = 59;
		int result = 1;
		result = result * PRIME + (this.isEnabled() ? 79 : 97);
		final Object $providerId = this.getProviderId();
		result = result * PRIME + ($providerId == null ? 43 : $providerId.hashCode());
		final Object $clientId = this.getClientId();
		result = result * PRIME + ($clientId == null ? 43 : $clientId.hashCode());
		final Object $clientSecret = this.getClientSecret();
		result = result * PRIME + ($clientSecret == null ? 43 : $clientSecret.hashCode());
		final Object $issuerUri = this.getIssuerUri();
		result = result * PRIME + ($issuerUri == null ? 43 : $issuerUri.hashCode());
		final Object $scope = this.getScope();
		result = result * PRIME + ($scope == null ? 43 : $scope.hashCode());
		return result;
	}

	@Override
	public String toString() {
		return "OAuthProviderConfig(providerId=" + this.getProviderId() + ", enabled=" + this.isEnabled() + ", clientId=" + this.getClientId() + ", clientSecret=" + this.getClientSecret() + ", issuerUri=" + this.getIssuerUri() + ", scope=" + this.getScope() + ")";
	}
}
