package com.src.main.subscription.security;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

public class HeaderSubscriptionTenantResolver implements SubscriptionTenantResolver {

	private final String headerName;

	public HeaderSubscriptionTenantResolver(String headerName) {
		this.headerName = headerName;
	}

	@Override
	public Long resolveTenantId() {
		ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
		if (attributes == null) {
			return null;
		}
		HttpServletRequest request = attributes.getRequest();
		String value = request == null ? null : request.getHeader(headerName);
		if (value == null || value.isBlank()) {
			return null;
		}
		return Long.valueOf(value.trim());
	}
}
