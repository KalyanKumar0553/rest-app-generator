package com.src.main.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RetryResponseDTO {
	private Long executionId;
	private String status;
}