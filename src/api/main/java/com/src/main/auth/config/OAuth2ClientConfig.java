package com.src.main.auth.config;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.oauth2.client.CommonOAuth2Provider;
import org.springframework.security.oauth2.client.InMemoryOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;

@Configuration
public class OAuth2ClientConfig {

	@Bean
	@ConditionalOnExpression(
			"T(org.springframework.util.StringUtils).hasText('${oauth.google.client-id:}') && "
					+ "T(org.springframework.util.StringUtils).hasText('${oauth.google.client-secret:}')")
	public ClientRegistrationRepository clientRegistrationRepository(
			@Value("${oauth.google.client-id}") String googleClientId,
			@Value("${oauth.google.client-secret}") String googleClientSecret,
			@Value("${oauth.google.scope:openid,profile,email}") String googleScope) {
		String[] scopes = Arrays.stream(googleScope.split(","))
				.map(String::trim)
				.filter(scope -> !scope.isEmpty())
				.toArray(String[]::new);
		ClientRegistration googleRegistration = CommonOAuth2Provider.GOOGLE
				.getBuilder("google")
				.clientId(googleClientId)
				.clientSecret(googleClientSecret)
				.scope(scopes)
				.build();
		return new InMemoryClientRegistrationRepository(googleRegistration);
	}

	@Bean
	@ConditionalOnExpression(
			"T(org.springframework.util.StringUtils).hasText('${oauth.google.client-id:}') && "
					+ "T(org.springframework.util.StringUtils).hasText('${oauth.google.client-secret:}')")
	public OAuth2AuthorizedClientService authorizedClientService(
			ClientRegistrationRepository clientRegistrationRepository) {
		return new InMemoryOAuth2AuthorizedClientService(clientRegistrationRepository);
	}
}
