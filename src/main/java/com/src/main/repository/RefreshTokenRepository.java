package com.src.main.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.src.main.model.RefreshToken;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, String> {
    Optional<RefreshToken> findByToken(String token);
    void deleteByUsername(String username);
}
