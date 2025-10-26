package com.src.main.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectStatusResponse {
	private String projectId;
	private String artifact;
	private String status;
	private String error;
}