package com.src.main.auth.repository;

import java.time.Instant;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.src.main.auth.model.InvalidatedToken;

public interface InvalidatedTokenRepository extends JpaRepository<InvalidatedToken, UUID> {
	boolean existsByToken(String token);

	long deleteByExpiresAtBefore(Instant now);
}
