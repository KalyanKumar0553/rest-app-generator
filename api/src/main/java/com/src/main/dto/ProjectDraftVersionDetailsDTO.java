package com.src.main.dto;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

public record ProjectDraftVersionDetailsDTO(
		UUID id,
		Integer draftVersion,
		String generator,
		String createdByUserId,
		UUID restoredFromVersionId,
		OffsetDateTime createdAt,
		String yaml,
		Map<String, Object> draftData) {
}
