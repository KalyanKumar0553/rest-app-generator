package com.src.main.auth.security;

import java.io.IOException;
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
		char separator = failureRedirectUri.contains("?") ? '&' : '?';
		response.sendRedirect(failureRedirectUri + separator + "error=" + URLEncoder.encode(message, StandardCharsets.UTF_8));
	}
}
