package com.src.main.cdn.dto;

import java.util.List;
import java.util.UUID;

public record CdnImageTriggerResponseDTO(
		List<UUID> queuedDraftIds,
		int queuedCount) {
}
