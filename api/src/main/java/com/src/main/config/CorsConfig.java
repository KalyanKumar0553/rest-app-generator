package com.src.main.config;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

	@Value("${app.cors.allowed-origins:http://localhost:4200}")
	private String allowedOrigins;

	@Override
	public void addCorsMappings(CorsRegistry registry) {
		List<String> origins = Arrays.stream(allowedOrigins.split(",")).map(String::trim).filter(value -> !value.isEmpty())
				.toList();

		registry.addMapping("/api/**").allowedOrigins(origins.toArray(new String[0]))
				.allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS").allowedHeaders("*")
				.exposedHeaders("Content-Disposition").allowCredentials(true).maxAge(3600);
	}
}
