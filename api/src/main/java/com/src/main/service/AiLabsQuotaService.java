package com.src.main.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.src.main.exception.GenericException;
import com.src.main.repository.AiLabsUsageRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AiLabsQuotaService {

	public static final String AI_LABS_USAGE_LIMIT_KEY = "app.feature.ai-labs.usage-limit";
	public static final String LIMIT_REACHED_MESSAGE = "Limit reached please subscribe for more Usage";

	private final ConfigMetadataService configMetadataService;
	private final AiLabsUsageRepository aiLabsUsageRepository;

	@Transactional(readOnly = true)
	public AiLabsQuotaSnapshot getSnapshot(String ownerUserId) {
		int limit = resolveUsageLimit();
		int usedCount = resolveUsageCount(ownerUserId);
		return buildSnapshot(limit, usedCount);
	}

	@Transactional
	public AiLabsQuotaSnapshot reserveUsage(String ownerUserId) {
		int limit = resolveUsageLimit();
		aiLabsUsageRepository.insertIfAbsent(ownerUserId);
		int updatedRows = limit < 0
				? aiLabsUsageRepository.incrementUsage(ownerUserId)
				: aiLabsUsageRepository.incrementUsageIfBelowLimit(ownerUserId, limit);
		if (updatedRows == 0) {
			throw new GenericException(HttpStatus.BAD_REQUEST, LIMIT_REACHED_MESSAGE);
		}
		return getSnapshot(ownerUserId);
	}

	private AiLabsQuotaSnapshot buildSnapshot(int limit, int usedCount) {
		boolean unlimited = limit < 0;
		int remainingCount = unlimited ? -1 : Math.max(limit - usedCount, 0);
		boolean limitReached = !unlimited && usedCount >= limit;
		return new AiLabsQuotaSnapshot(limit, usedCount, remainingCount, unlimited, limitReached);
	}

	private int resolveUsageLimit() {
		return configMetadataService.getPropertyCurrentIntValue(AI_LABS_USAGE_LIMIT_KEY).orElse(5);
	}

	private int resolveUsageCount(String ownerUserId) {
		return aiLabsUsageRepository.findById(ownerUserId)
				.map(entity -> Math.max(entity.getUsageCount(), 0))
				.orElse(0);
	}

	public record AiLabsQuotaSnapshot(int usageLimit, int usedCount, int remainingCount, boolean unlimited, boolean limitReached) {
		public Integer usageLimitValue() {
			return unlimited ? null : usageLimit;
		}

		public Integer remainingCountValue() {
			return unlimited ? null : remainingCount;
		}
	}
}
