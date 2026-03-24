package com.src.main.subscription.dto;

import java.time.LocalDateTime;

import com.src.main.subscription.enums.FeatureType;
import com.src.main.subscription.enums.ResetPolicy;
import com.src.main.subscription.enums.ValueDataType;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FeatureResponse {
	private Long id;
	private String code;
	private String name;
	private String description;
	private FeatureType featureType;
	private ValueDataType valueDataType;
	private String unit;
	private ResetPolicy resetPolicy;
	private Boolean isActive;
	private Boolean isSystem;
	private String metadataJson;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
}
