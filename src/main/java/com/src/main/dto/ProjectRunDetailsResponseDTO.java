package com.src.main.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.src.main.util.ProjectRunStatus;
import com.src.main.util.ProjectRunType;

public record ProjectRunDetailsResponseDTO(
        UUID runId,
        UUID projectId,
        String ownerId,
        ProjectRunType type,
        ProjectRunStatus status,
        int runNumber,
        String errorMessage,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {}
