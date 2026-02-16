package com.src.main.config;

import static org.springframework.security.config.Customizer.withDefaults;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.Ordered;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class RestAppGeneratorSecurityConfig {

	@Bean
	@Order(Ordered.HIGHEST_PRECEDENCE)
	public SecurityFilterChain publicProjectCreateSecurityFilterChain(HttpSecurity http) throws Exception {
		http.securityMatcher("/api/projects/**", "/api/runs/**", "/api/openapi/**", "/api/project-view/**",
				"/api/analytics/**")
				.authorizeHttpRequests(authorize -> authorize
						.requestMatchers(HttpMethod.OPTIONS, "/api/**").permitAll()
						.requestMatchers(HttpMethod.POST, "/api/project-view/generate-zip").permitAll()
						.requestMatchers(HttpMethod.POST, "/api/analytics/visits/home").permitAll()
						.requestMatchers(HttpMethod.GET, "/api/openapi/**").permitAll()
						.anyRequest().authenticated())
				.csrf(csrf -> csrf.disable()).cors(withDefaults());

		return http.build();
	}
}
