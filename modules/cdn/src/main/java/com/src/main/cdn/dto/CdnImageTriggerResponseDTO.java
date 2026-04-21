package com.src.main.cdn.dto;

import java.util.List;
import java.util.UUID;

public record CdnImageTriggerResponseDTO(
		List<UUID> processedDraftIds,
		int processedCount,
		List<CdnImageTriggerFailureDTO> failures) {
}
