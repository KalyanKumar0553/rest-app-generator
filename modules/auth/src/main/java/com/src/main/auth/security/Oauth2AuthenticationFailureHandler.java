package com.src.main.auth.security;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class Oauth2AuthenticationFailureHandler implements AuthenticationFailureHandler {
	private static final String OAUTH_CALLBACK_PATH = "/auth/oauth/callback";
	private final String failureRedirectUri;

	public Oauth2AuthenticationFailureHandler(
			@Value("${app.oauth2.failure-redirect-uri:http://localhost:4200/#/auth/oauth/callback}") String failureRedirectUri) {
		this.failureRedirectUri = failureRedirectUri;
	}

	@Override
	public void onAuthenticationFailure(
			HttpServletRequest request,
			HttpServletResponse response,
			AuthenticationException exception) throws IOException, ServletException {
		String message = exception == null ? "OAuth sign-in failed." : exception.getMessage();
		String redirectUri = normalizeRedirectUri(failureRedirectUri);
		char separator = redirectUri.contains("?") ? '&' : '?';
		response.sendRedirect(redirectUri + separator + "error=" + URLEncoder.encode(message, StandardCharsets.UTF_8));
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
}
