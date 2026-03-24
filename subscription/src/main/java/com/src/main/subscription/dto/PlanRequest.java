package com.src.main.subscription.dto;

import com.src.main.subscription.enums.PlanType;
import com.src.main.subscription.enums.PlanVisibility;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PlanRequest {
	@NotBlank
	private String code;
	@NotBlank
	private String name;
	private String description;
	private Boolean isActive = Boolean.TRUE;
	private Boolean isDefault = Boolean.FALSE;
	private Integer sortOrder = 0;
	private Integer trialDays;
	@NotNull
	private PlanType planType;
	@NotNull
	private PlanVisibility visibility;
	private Integer maxUsers;
	private Integer maxProjects;
	private Integer maxStorageMb;
	private String metadataJson;
}
