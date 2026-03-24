package com.src.main.dto;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

public record ArtifactAppVersionResponseDTO(
		UUID id,
		String versionCode,
		boolean published,
		String createdByUserId,
		Map<String, Object> config,
		OffsetDateTime createdAt,
		OffsetDateTime updatedAt) {
}
