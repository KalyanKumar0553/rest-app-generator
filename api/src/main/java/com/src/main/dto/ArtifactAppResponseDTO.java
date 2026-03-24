package com.src.main.dto;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record ArtifactAppResponseDTO(
		UUID id,
		String code,
		String name,
		String description,
		String status,
		String ownerUserId,
		String generatorLanguage,
		String buildTool,
		List<String> enabledPacks,
		Map<String, Object> config,
		String publishedVersion,
		int versionCount,
		OffsetDateTime createdAt,
		OffsetDateTime updatedAt) {
}
