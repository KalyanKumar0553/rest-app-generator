package com.src.main.auth.repository;

import java.time.Instant;

import org.springframework.data.jpa.repository.JpaRepository;

import com.src.main.auth.model.CaptchaChallenge;

public interface CaptchaChallengeRepository extends JpaRepository<CaptchaChallenge, String> {
	long deleteByUsedTrueOrExpiresAtBefore(Instant now);
}
