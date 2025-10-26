package com.src.main.web.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StepSummary {
	private String stepName;
	private String status;
	private long readCount; // was int
	private long writeCount; // was int
	private java.time.LocalDateTime startTime;
	private java.time.LocalDateTime endTime;
	private String exitStatus;
}