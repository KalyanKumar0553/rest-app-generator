package com.src.main.cdn.dto;

import java.time.OffsetDateTime;

public record CdnImageBatchSettingsResponseDTO(
		boolean batchProcessingEnabled,
		String updatedByUserId,
		OffsetDateTime updatedAt) {
}
