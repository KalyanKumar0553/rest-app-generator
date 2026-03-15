package com.src.main.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StepSummaryDTO {
	private String stepName;
	private String status;
	private long readCount;
	private long writeCount;
	private LocalDateTime startTime;
	private LocalDateTime endTime;
	private String exitStatus;
}