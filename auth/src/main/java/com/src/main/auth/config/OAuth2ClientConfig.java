package com.src.main.auth.config;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.oauth2.client.CommonOAuth2Provider;
import org.springframework.security.oauth2.client.InMemoryOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;

import com.src.main.auth.service.OAuthProviderConfigService;

@Configuration
public class OAuth2ClientConfig {

	@Bean
	public ClientRegistrationRepository clientRegistrationRepository(
			@Value("${oauth.google.client-id}") String googleClientId,
			@Value("${oauth.google.client-secret}") String googleClientSecret,
			@Value("${oauth.google.scope:openid,profile,email}") String googleScope,
			OAuthProviderConfigService oAuthProviderConfigService) {
		Map<String, ClientRegistration> registrations = new LinkedHashMap<>();

		if (hasText(googleClientId) && hasText(googleClientSecret)) {
			ClientRegistration googleRegistration = CommonOAuth2Provider.GOOGLE
					.getBuilder("google")
					.clientId(googleClientId)
					.clientSecret(googleClientSecret)
					.scope(parseScopes(googleScope))
					.build();
			registrations.put(googleRegistration.getRegistrationId(), googleRegistration);
		}

		oAuthProviderConfigService.getEnabledKeycloakSettings().ifPresent(settings -> {
			ClientRegistration keycloakRegistration = ClientRegistration.withRegistrationId("keycloak")
					.clientId(settings.clientId())
					.clientSecret(settings.clientSecret())
					.scope(parseScopes(settings.scope()))
					.authorizationGrantType(org.springframework.security.oauth2.core.AuthorizationGrantType.AUTHORIZATION_CODE)
					.redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
					.issuerUri(settings.issuerUri())
					.userNameAttributeName("preferred_username")
					.clientName("Keycloak")
					.build();
			registrations.put(keycloakRegistration.getRegistrationId(), keycloakRegistration);
		});

		return new InMemoryClientRegistrationRepository(List.copyOf(registrations.values()));
	}

	@Bean
	public OAuth2AuthorizedClientService authorizedClientService(
			ClientRegistrationRepository clientRegistrationRepository) {
		return new InMemoryOAuth2AuthorizedClientService(clientRegistrationRepository);
	}

	private String[] parseScopes(String scopeText) {
		return Arrays.stream(scopeText.split(","))
				.map(String::trim)
				.filter(scope -> !scope.isEmpty())
				.toArray(String[]::new);
	}

	private boolean hasText(String value) {
		return value != null && !value.trim().isEmpty();
	}
}
