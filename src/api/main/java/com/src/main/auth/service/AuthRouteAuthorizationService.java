package com.src.main.auth.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.src.main.auth.model.AuthRoute;
import com.src.main.auth.model.RoleType;
import com.src.main.auth.repository.AuthRouteRepository;

@Service
public class AuthRouteAuthorizationService {
	public record ProtectedRoute(String pathPattern, String httpMethod, int priority, List<String> roles) {}

	private final AuthRouteRepository authRouteRepository;

	public AuthRouteAuthorizationService(AuthRouteRepository authRouteRepository) {
		this.authRouteRepository = authRouteRepository;
	}

	public List<ProtectedRoute> getProtectedRoutes() {
		List<AuthRoute> routes = authRouteRepository.findActiveRoutesByRoleType(RoleType.AUTH_ROLE);
		Map<String, ProtectedRoute> groupedRoutes = new LinkedHashMap<>();
		for (AuthRoute route : routes) {
			String method = normalizeMethod(route.getHttpMethod());
			String key = route.getPriority() + "|" + method + "|" + route.getPathPattern();
			ProtectedRoute existing = groupedRoutes.get(key);
			if (existing == null) {
				List<String> roles = new ArrayList<>();
				roles.add(route.getRoleName());
				groupedRoutes.put(key, new ProtectedRoute(route.getPathPattern(), method, route.getPriority(), roles));
				continue;
			}
			if (!existing.roles().contains(route.getRoleName())) {
				existing.roles().add(route.getRoleName());
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
