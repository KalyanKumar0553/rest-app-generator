package com.src.main.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

import com.src.main.security.RateLimitFilter;

@Configuration
public class RateLimitFilterConfig {

	@Bean
	public RateLimitFilter rateLimitFilter() {
		return new RateLimitFilter();
	}

	@Bean
	public FilterRegistrationBean<RateLimitFilter> rateLimitFilterRegistration(RateLimitFilter rateLimitFilter) {
		FilterRegistrationBean<RateLimitFilter> registration = new FilterRegistrationBean<>();
		registration.setFilter(rateLimitFilter);
		registration.addUrlPatterns("/api/*");
		registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
		return registration;
	}
}
