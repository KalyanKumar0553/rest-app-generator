package com.src.main.auth.security;

import java.io.IOException;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import com.src.main.auth.repository.InvalidatedTokenRepository;
import com.src.main.auth.service.RbacService;
import com.src.main.auth.util.JwtClaims;
import com.src.main.auth.util.JwtUtils;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class JwtAuthenticationFilter extends OncePerRequestFilter {
	private final JwtUtils jwtUtils;
	private final InvalidatedTokenRepository invalidatedTokenRepository;
	private final RbacService rbacService;

	public JwtAuthenticationFilter(JwtUtils jwtUtils, InvalidatedTokenRepository invalidatedTokenRepository, RbacService rbacService) {
		this.jwtUtils = jwtUtils;
		this.invalidatedTokenRepository = invalidatedTokenRepository;
		this.rbacService = rbacService;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		String token = resolveToken(request);
		if (token != null && !token.isBlank()) {
			if (invalidatedTokenRepository.existsByToken(token)) {
				SecurityContextHolder.clearContext();
				filterChain.doFilter(request, response);
				return;
			}
			try {
				JwtClaims claims = jwtUtils.parse(token);
				Date expiration = claims.getExpiration();
				if (expiration != null) {
					DateTimeFormatter formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
					String formatted = expiration.toInstant().atZone(ZoneId.systemDefault()).format(formatter);
					response.setHeader("X-Token-Expires-At", formatted);
				}
				RbacService.AccessProfile accessProfile = rbacService.getAccessProfile(claims.getSub());
				LinkedHashSet<String> authorityNames = new LinkedHashSet<>();
				authorityNames.addAll(accessProfile.roles());
				authorityNames.addAll(accessProfile.permissions());
				if (authorityNames.isEmpty()) {
					if (claims.getRoles() != null) {
						authorityNames.addAll(claims.getRoles());
					}
					if (claims.getPermissions() != null) {
						authorityNames.addAll(claims.getPermissions());
					}
				}
				List<SimpleGrantedAuthority> authorities = authorityNames.isEmpty()
						? Collections.emptyList()
						: authorityNames.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
				UsernamePasswordAuthenticationToken auth =
						new UsernamePasswordAuthenticationToken(claims.getSub(), token, authorities);
				SecurityContextHolder.getContext().setAuthentication(auth);
			} catch (Exception ignored) {
				SecurityContextHolder.clearContext();
			}
		}
		filterChain.doFilter(request, response);
	}

	private String resolveToken(HttpServletRequest request) {
		String header = request.getHeader(HttpHeaders.AUTHORIZATION);
		if (header != null && header.startsWith("Bearer ")) {
			return header.substring(7);
		}
		String queryToken = request.getParameter("access_token");
		if (queryToken != null && !queryToken.isBlank()) {
			return queryToken.trim();
		}
		String legacyQueryToken = request.getParameter("accessToken");
		if (legacyQueryToken != null && !legacyQueryToken.isBlank()) {
			return legacyQueryToken.trim();
		}
		return null;
	}
}
