package com.src.main.subscription.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.src.main.subscription.enums.PlanType;
import com.src.main.subscription.enums.PlanVisibility;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PlanResponse {
	private Long id;
	private String code;
	private String name;
	private String description;
	private Boolean isActive;
	private Boolean isDefault;
	private Integer sortOrder;
	private Integer trialDays;
	private PlanType planType;
	private PlanVisibility visibility;
	private Integer maxUsers;
	private Integer maxProjects;
	private Integer maxStorageMb;
	private List<String> roleNames;
	private String metadataJson;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
}
