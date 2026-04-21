package com.src.main.auth.security;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.src.main.auth.dto.response.TokenPairResponseDto;
import com.src.main.auth.service.AuthService;
import com.src.main.auth.service.OauthService;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class Oauth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {
	private static final String OAUTH_CALLBACK_PATH = "/auth/oauth/callback";
	private final OauthService oauthService;
	private final AuthService authService;
	private final String successRedirectUri;

	public Oauth2AuthenticationSuccessHandler(
			OauthService oauthService,
			AuthService authService,
			@Value("${app.oauth2.success-redirect-uri:http://localhost:4200/#/auth/oauth/callback}") String successRedirectUri) {
		this.oauthService = oauthService;
		this.authService = authService;
		this.successRedirectUri = successRedirectUri;
	}

	@Override
	public void onAuthenticationSuccess(
			HttpServletRequest request,
			HttpServletResponse response,
			Authentication authentication) throws IOException, ServletException {
		String redirectUri = normalizeRedirectUri(successRedirectUri);
		if (!(authentication.getPrincipal() instanceof OAuth2User oauthUser)) {
			response.sendRedirect(appendQuery(redirectUri, "error", "Unsupported OAuth principal"));
			return;
		}

		String registrationId = authentication instanceof OAuth2AuthenticationToken token
				? token.getAuthorizedClientRegistrationId()
				: "oauth";
		var principal = oauthService.extractOauthPrincipal(oauthUser.getAttributes());
		String userId = oauthService.upsertOauthUser(principal);
		TokenPairResponseDto tokenPair = authService.loginWithUserId(userId);
		response.sendRedirect(buildSuccessRedirect(redirectUri, tokenPair, registrationId));
	}

	private String buildSuccessRedirect(String redirectUri, TokenPairResponseDto tokenPair, String registrationId) {
		String redirect = redirectUri;
		redirect = appendQuery(redirect, "provider", registrationId);
		redirect = appendQuery(redirect, "accessToken", tokenPair.getAccessToken());
		redirect = appendQuery(redirect, "refreshToken", tokenPair.getRefreshToken());

		var user = tokenPair.getUser();
		if (user != null) {
			redirect = appendQuery(redirect, "userId", user.getId());
			redirect = appendQuery(redirect, "email", user.getEmail());
			redirect = appendQuery(redirect, "name", user.getName());
			redirect = appendQuery(redirect, "role", user.getRole());
		}
		return redirect;
	}

	private String normalizeRedirectUri(String uri) {
		if (uri == null || uri.isBlank() || uri.contains("#/")) {
			return uri;
		}
		try {
			URI parsed = URI.create(uri.trim());
			String path = parsed.getPath() == null ? "" : parsed.getPath().trim();
			if (!path.endsWith(OAUTH_CALLBACK_PATH)) {
				return uri;
			}
			StringBuilder normalized = new StringBuilder();
			normalized.append(parsed.getScheme()).append("://").append(parsed.getRawAuthority());
			normalized.append("/#").append(OAUTH_CALLBACK_PATH);
			if (parsed.getRawQuery() != null && !parsed.getRawQuery().isBlank()) {
				normalized.append('?').append(parsed.getRawQuery());
			}
			return normalized.toString();
		} catch (IllegalArgumentException ex) {
			return uri;
		}
	}

	private String appendQuery(String uri, String key, String value) {
		if (value == null || value.isBlank()) {
			return uri;
		}
		char separator = uri.contains("?") ? '&' : '?';
		return uri + separator + key + "=" + URLEncoder.encode(value, StandardCharsets.UTF_8);
	}
}
