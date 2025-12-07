package com.src.main.dto;

import com.src.main.model.ProjectEntity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectSummaryDTO {
	private String id;
	private String name;
	private String description;
	private String createdAt;
	private String updatedAt;
	private String status;

	public static ProjectSummaryDTO toDTO(ProjectEntity entity) {
		if (entity == null) {
			return null;
		}
		return new ProjectSummaryDTO(entity.getId() != null ? entity.getId().toString() : null, entity.getName(),
				entity.getDescription(), entity.getCreatedAt() != null ? entity.getCreatedAt().toString() : null,
				entity.getUpdatedAt() != null ? entity.getUpdatedAt().toString() : null, entity.getStatus());
	}

}
