package com.src.main.subscription.dto;

import com.src.main.subscription.enums.FeatureType;
import com.src.main.subscription.enums.ResetPolicy;
import com.src.main.subscription.enums.ValueDataType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FeatureRequest {
	@NotBlank
	private String code;
	@NotBlank
	private String name;
	private String description;
	@NotNull
	private FeatureType featureType;
	private ValueDataType valueDataType;
	private String unit;
	private ResetPolicy resetPolicy;
	private Boolean isActive = Boolean.TRUE;
	private Boolean isSystem = Boolean.FALSE;
	private String metadataJson;
}
