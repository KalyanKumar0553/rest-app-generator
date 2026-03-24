package com.src.main.subscription.entity;

import com.src.main.subscription.enums.PlanType;
import com.src.main.subscription.enums.PlanVisibility;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "subscription_plan")
@Getter
@Setter
public class SubscriptionPlanEntity extends BaseSubscriptionEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "code", nullable = false, length = 100, unique = true)
	private String code;

	@Column(name = "name", nullable = false, length = 150)
	private String name;

	@Column(name = "description", length = 1000)
	private String description;

	@Column(name = "is_active", nullable = false)
	private Boolean isActive = Boolean.TRUE;

	@Column(name = "is_default", nullable = false)
	private Boolean isDefault = Boolean.FALSE;

	@Column(name = "sort_order")
	private Integer sortOrder = 0;

	@Column(name = "trial_days")
	private Integer trialDays;

	@Enumerated(EnumType.STRING)
	@Column(name = "plan_type", nullable = false, length = 50)
	private PlanType planType;

	@Enumerated(EnumType.STRING)
	@Column(name = "visibility", nullable = false, length = 50)
	private PlanVisibility visibility;

	@Column(name = "max_users")
	private Integer maxUsers;

	@Column(name = "max_projects")
	private Integer maxProjects;

	@Column(name = "max_storage_mb")
	private Integer maxStorageMb;

	@Column(name = "metadata_json", columnDefinition = "text")
	private String metadataJson;
}
