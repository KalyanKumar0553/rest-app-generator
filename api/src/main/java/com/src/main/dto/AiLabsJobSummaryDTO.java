package com.src.main.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record AiLabsJobSummaryDTO(
		UUID jobId,
		String generatedBy,
		String status,
		String generator,
		String projectId,
		OffsetDateTime generatedOn,
		OffsetDateTime updatedAt) {
}
