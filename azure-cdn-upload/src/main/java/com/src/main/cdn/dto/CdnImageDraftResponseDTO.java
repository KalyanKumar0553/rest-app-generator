package com.src.main.cdn.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.src.main.cdn.model.CdnImageUploadStatus;

public record CdnImageDraftResponseDTO(
		UUID id,
		String fileName,
		String contentType,
		long sizeBytes,
		CdnImageUploadStatus status,
		int attemptCount,
		String lastErrorMessage,
		OffsetDateTime createdAt,
		OffsetDateTime updatedAt,
		OffsetDateTime queuedAt,
		OffsetDateTime completedAt,
		String imageUrl) {
}
