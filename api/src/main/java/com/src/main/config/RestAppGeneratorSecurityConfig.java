package com.src.main.config;

import static org.springframework.security.config.Customizer.withDefaults;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.Ordered;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.src.main.auth.repository.InvalidatedTokenRepository;
import com.src.main.auth.service.RbacService;
import com.src.main.auth.security.JwtAuthenticationFilter;
import com.src.main.auth.util.JwtUtils;

@Configuration
public class RestAppGeneratorSecurityConfig {

	@Bean
	@Order(Ordered.HIGHEST_PRECEDENCE)
	public SecurityFilterChain publicProjectCreateSecurityFilterChain(
			HttpSecurity http,
			InvalidatedTokenRepository invalidatedTokenRepository,
			JwtUtils jwtUtils,
			RbacService rbacService) throws Exception {
		AuthenticationEntryPoint unauthorizedEntryPoint = new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED);
		http.securityMatcher("/api/projects/**", "/api/runs/**", "/api/openapi/**", "/api/project-view/**",
				"/api/analytics/**", "/api/newsletter/**")
				.authorizeHttpRequests(authorize -> authorize
						.requestMatchers(HttpMethod.OPTIONS, "/api/**").permitAll()
						.requestMatchers(HttpMethod.POST, "/api/project-view/generate-zip").permitAll()
						.requestMatchers(HttpMethod.GET, "/api/projects/tab-details").permitAll()
						.requestMatchers(HttpMethod.POST, "/api/analytics/visits/home").permitAll()
						.requestMatchers(HttpMethod.POST, "/api/v1/orders").permitAll()
						.requestMatchers(HttpMethod.POST, "/api/v1/payments/webhook/**").permitAll()
						.requestMatchers(HttpMethod.POST, "/api/newsletter/subscriptions").permitAll()
						.requestMatchers(HttpMethod.GET, "/api/openapi/**").permitAll()
						.anyRequest().authenticated())
				.csrf(csrf -> csrf.disable())
				.cors(withDefaults())
				.sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.exceptionHandling(ex -> ex.authenticationEntryPoint(unauthorizedEntryPoint))
				.addFilterBefore(new JwtAuthenticationFilter(jwtUtils, invalidatedTokenRepository, rbacService),
						UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}
}
