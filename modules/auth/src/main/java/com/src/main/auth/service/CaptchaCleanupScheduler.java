package com.src.main.auth.service;

import java.time.Instant;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.src.main.auth.repository.CaptchaChallengeRepository;

@Service
public class CaptchaCleanupScheduler {
	private final CaptchaChallengeRepository captchaChallengeRepository;

	public CaptchaCleanupScheduler(CaptchaChallengeRepository captchaChallengeRepository) {
		this.captchaChallengeRepository = captchaChallengeRepository;
	}

	@Scheduled(fixedDelayString = "${captcha.cleanup.ms:3600000}")
	@Transactional
	public void cleanupExpiredAndUsed() {
		captchaChallengeRepository.deleteByUsedTrueOrExpiresAtBefore(Instant.now());
	}
}
