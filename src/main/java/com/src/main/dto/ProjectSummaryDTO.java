package com.src.main.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectSummaryDTO {
	private String projectId;
	private String artifact;
	private String status;
}