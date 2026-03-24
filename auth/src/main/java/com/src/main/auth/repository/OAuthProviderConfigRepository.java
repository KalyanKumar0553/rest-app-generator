package com.src.main.auth.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.src.main.auth.model.OAuthProviderConfig;

public interface OAuthProviderConfigRepository extends JpaRepository<OAuthProviderConfig, String> {
	Optional<OAuthProviderConfig> findByProviderIdIgnoreCase(String providerId);
}
