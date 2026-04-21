package com.src.main.auth.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.src.main.auth.model.AuthRoute;
import com.src.main.auth.repository.AuthRouteRepository;

@Service
public class AuthRouteAuthorizationService {
	public record ProtectedRoute(String pathPattern, String httpMethod, int priority, List<String> authorities) {}

	private final AuthRouteRepository authRouteRepository;

	public AuthRouteAuthorizationService(AuthRouteRepository authRouteRepository) {
		this.authRouteRepository = authRouteRepository;
	}

	public List<ProtectedRoute> getProtectedRoutes() {
		List<AuthRoute> routes = authRouteRepository.findActiveRoutes();
		Map<String, ProtectedRoute> groupedRoutes = new LinkedHashMap<>();
		for (AuthRoute route : routes) {
			String method = normalizeMethod(route.getHttpMethod());
			String key = route.getPriority() + "|" + method + "|" + route.getPathPattern();
			ProtectedRoute existing = groupedRoutes.get(key);
			if (existing == null) {
				List<String> authorities = new ArrayList<>();
				authorities.add(route.getAuthorityName());
				groupedRoutes.put(key, new ProtectedRoute(route.getPathPattern(), method, route.getPriority(), authorities));
				continue;
			}
			if (!existing.authorities().contains(route.getAuthorityName())) {
				existing.authorities().add(route.getAuthorityName());
			}
		}
		return new ArrayList<>(groupedRoutes.values());
	}

	private String normalizeMethod(String httpMethod) {
		if (httpMethod == null || httpMethod.isBlank()) {
			return null;
		}
		return httpMethod.trim().toUpperCase();
	}
}
