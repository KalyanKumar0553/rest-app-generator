package com.src.main.auth.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import com.src.main.auth.repository.InvalidatedTokenRepository;
import com.src.main.auth.service.AuthRouteAuthorizationService;
import com.src.main.auth.security.JwtAuthenticationFilter;
import com.src.main.auth.security.Oauth2AuthenticationFailureHandler;
import com.src.main.auth.security.Oauth2AuthenticationSuccessHandler;
import com.src.main.auth.util.JwtUtils;

@Configuration
@EnableMethodSecurity
@org.springframework.core.annotation.Order(3)
public class SecurityConfig {
	@Value("${jwt.secret:change-me}")
	private String jwtSecret;

	@Value("${jwt.issuer:auth-service}")
	private String jwtIssuer;

	@Bean
	public JwtUtils jwtUtils() {
		return new JwtUtils(jwtIssuer, jwtSecret);
	}

	@Bean
	public SecurityFilterChain filterChain(
			HttpSecurity http,
			InvalidatedTokenRepository invalidatedTokenRepository,
			JwtUtils jwtUtils,
			AuthRouteAuthorizationService authRouteAuthorizationService,
			ObjectProvider<ClientRegistrationRepository> clientRegistrationRepositoryProvider,
			Oauth2AuthenticationSuccessHandler oauth2AuthenticationSuccessHandler,
			Oauth2AuthenticationFailureHandler oauth2AuthenticationFailureHandler) throws Exception {
		ClientRegistrationRepository clientRegistrationRepository = clientRegistrationRepositoryProvider.getIfAvailable();
		boolean oauthEnabled = clientRegistrationRepository != null
				&& (clientRegistrationRepository.findByRegistrationId("google") != null
						|| clientRegistrationRepository.findByRegistrationId("keycloak") != null);
		http.csrf(csrf -> csrf.disable())
				.sessionManagement(sm -> sm.sessionCreationPolicy(org.springframework.security.config.http.SessionCreationPolicy.IF_REQUIRED))
				.authorizeHttpRequests(auth -> {
					auth.requestMatchers("/health", "/actuator/health").permitAll();
					auth.requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll();
					auth.requestMatchers(
							new AntPathRequestMatcher("/assets/**"),
							new AntPathRequestMatcher("/static/**"),
							new AntPathRequestMatcher("/webjars/**"),
							new AntPathRequestMatcher("/**/*.css"),
							new AntPathRequestMatcher("/**/*.js"),
							new AntPathRequestMatcher("/**/*.map"),
							new AntPathRequestMatcher("/**/*.png"),
							new AntPathRequestMatcher("/**/*.jpg"),
							new AntPathRequestMatcher("/**/*.jpeg"),
							new AntPathRequestMatcher("/**/*.gif"),
							new AntPathRequestMatcher("/**/*.svg"),
							new AntPathRequestMatcher("/**/*.woff"),
							new AntPathRequestMatcher("/**/*.woff2"),
							new AntPathRequestMatcher("/**/*.ttf"),
							new AntPathRequestMatcher("/**/*.eot"))
							.permitAll();
					auth.requestMatchers("/oauth2/**", "/login/oauth2/**").permitAll();
					auth.requestMatchers("/api/openapi/**").permitAll();
					auth.requestMatchers(
							"/api/v1/auth/captcha",
							"/api/v1/auth/signup",
							"/api/v1/auth/identifier/exists",
							"/api/v1/auth/otp/generate",
							"/api/v1/auth/otp/verify",
							"/api/v1/auth/password/forgot",
							"/api/v1/auth/password/reset",
							"/api/v1/auth/login",
							"/api/v1/auth/oauth/**",
							"/api/v1/auth/token/refresh",
							"/api/v1/auth/token/validate")
							.permitAll();
					auth.requestMatchers("/api/v1/admin/auth/login").permitAll();
					applyDynamicProtectedRoutes(auth, authRouteAuthorizationService);
					auth.anyRequest().authenticated();
				});

		if (oauthEnabled) {
			http.oauth2Login(oauth -> oauth
					.successHandler(oauth2AuthenticationSuccessHandler)
					.failureHandler(oauth2AuthenticationFailureHandler));
		}

		http.addFilterBefore(new JwtAuthenticationFilter(jwtUtils, invalidatedTokenRepository), UsernamePasswordAuthenticationFilter.class);
		return http.build();
	}

	private void applyDynamicProtectedRoutes(
			AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry auth,
			AuthRouteAuthorizationService authRouteAuthorizationService) {
		for (AuthRouteAuthorizationService.ProtectedRoute route : authRouteAuthorizationService.getProtectedRoutes()) {
			AntPathRequestMatcher matcher = route.httpMethod() == null
					? new AntPathRequestMatcher(route.pathPattern())
					: new AntPathRequestMatcher(route.pathPattern(), route.httpMethod());
			auth.requestMatchers(matcher).hasAnyAuthority(route.authorities().toArray(String[]::new));
		}
	}
}
