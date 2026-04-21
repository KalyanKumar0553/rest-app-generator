package com.src.main.cdn.dto;

import java.util.UUID;

public record CdnImageTriggerFailureDTO(UUID draftId, String message) {
}
