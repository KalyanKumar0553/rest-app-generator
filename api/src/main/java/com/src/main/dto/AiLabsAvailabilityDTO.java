package com.src.main.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiLabsAvailabilityDTO {

	private boolean enabled;
	private Integer usageLimit;
	private int usedCount;
	private Integer remainingCount;
	private boolean limitReached;
}
