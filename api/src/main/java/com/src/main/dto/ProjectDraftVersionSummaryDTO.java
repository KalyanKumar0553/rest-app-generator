package com.src.main.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ProjectDraftVersionSummaryDTO(
		UUID id,
		Integer draftVersion,
		String generator,
		String createdByUserId,
		UUID restoredFromVersionId,
		OffsetDateTime createdAt) {
}
