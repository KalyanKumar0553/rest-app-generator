package com.src.main.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectCreateResponse {
	private String projectId;
	private String status;
}