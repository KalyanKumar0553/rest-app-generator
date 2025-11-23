package com.src.main.mapper;

import com.src.main.dto.ProjectRunDetailsResponseDTO;
import com.src.main.model.ProjectRunEntity;

public final class ProjectRunMapper {

    private ProjectRunMapper() {
        // Utility class: no instances
    }

    public static ProjectRunDetailsResponseDTO toDto(ProjectRunEntity entity) {
        if (entity == null) {
            return null;
        }

        return new ProjectRunDetailsResponseDTO(
                entity.getId(),
                entity.getProject() != null ? entity.getProject().getId() : null,
                entity.getOwnerId(),
                entity.getType(),
                entity.getStatus(),
                entity.getRunNumber(),
                entity.getErrorMessage(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
