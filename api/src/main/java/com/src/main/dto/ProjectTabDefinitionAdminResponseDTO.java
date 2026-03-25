package com.src.main.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ProjectTabDefinitionAdminResponseDTO(
		UUID id,
		String key,
		String label,
		String icon,
		String componentKey,
		int order,
		String generatorLanguage,
		boolean enabled,
		OffsetDateTime createdAt,
		OffsetDateTime updatedAt) {
}
