package com.src.main.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectCreateResponseDTO {
	private String projectId;
	private String status;
}