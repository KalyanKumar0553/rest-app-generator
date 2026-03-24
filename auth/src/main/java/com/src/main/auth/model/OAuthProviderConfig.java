package com.src.main.auth.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "auth_oauth_provider_config")
@Data
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
}
