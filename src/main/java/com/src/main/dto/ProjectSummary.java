package com.src.main.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectSummary {
	private String projectId;
	private String artifact;
	private String status;
}