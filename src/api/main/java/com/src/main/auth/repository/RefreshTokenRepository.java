package com.src.main.auth.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.src.main.auth.model.RefreshToken;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, String> {
	Optional<RefreshToken> findFirstByIdAndRevokedFalseAndExpiresAtAfter(String id, Instant now);
	List<RefreshToken> findByFamilyId(String familyId);
	List<RefreshToken> findByUserId(String userId);
}
