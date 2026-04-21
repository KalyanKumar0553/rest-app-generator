package com.src.main.auth.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.src.main.auth.model.OAuthProviderConfig;
import com.src.main.auth.repository.OAuthProviderConfigRepository;

@Service
public class OAuthProviderConfigService {

	public record KeycloakSettings(
			String clientId,
			String clientSecret,
			String issuerUri,
			String scope) {
	}

	private final OAuthProviderConfigRepository repository;
	private final boolean keycloakFeatureEnabled;
	private final String googleClientId;
	private final String googleClientSecret;

	public OAuthProviderConfigService(
			OAuthProviderConfigRepository repository,
			@Value("${app.auth.keycloak.enabled:false}") boolean keycloakFeatureEnabled,
			@Value("${oauth.google.client-id:}") String googleClientId,
			@Value("${oauth.google.client-secret:}") String googleClientSecret) {
		this.repository = repository;
		this.keycloakFeatureEnabled = keycloakFeatureEnabled;
		this.googleClientId = googleClientId;
		this.googleClientSecret = googleClientSecret;
	}

	public boolean isGoogleOauthEnabled() {
		return hasText(googleClientId) && hasText(googleClientSecret);
	}

	public boolean isKeycloakFeatureEnabled() {
		return keycloakFeatureEnabled;
	}

	public Optional<KeycloakSettings> getEnabledKeycloakSettings() {
		if (!keycloakFeatureEnabled) {
			return Optional.empty();
		}
		return repository.findByProviderIdIgnoreCase("keycloak")
				.filter(OAuthProviderConfig::isEnabled)
				.filter(config -> hasText(config.getClientId()) && hasText(config.getClientSecret()) && hasText(config.getIssuerUri()))
				.map(config -> new KeycloakSettings(
						config.getClientId().trim(),
						config.getClientSecret().trim(),
						config.getIssuerUri().trim(),
						hasText(config.getScope()) ? config.getScope().trim() : "openid,profile,email"));
	}

	public boolean isKeycloakOauthEnabled() {
		return getEnabledKeycloakSettings().isPresent();
	}

	private boolean hasText(String value) {
		return value != null && !value.trim().isEmpty();
	}
}
