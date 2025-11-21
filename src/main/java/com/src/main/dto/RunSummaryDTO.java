package com.src.main.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RunSummaryDTO {
	private Long executionId;
	private String status;
	private LocalDateTime startTime;
	private LocalDateTime endTime;
}