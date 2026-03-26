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

@Entity
@Table(name = "subscription_plan")
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

	public Long getId() {
		return this.id;
	}

	public String getCode() {
		return this.code;
	}

	public String getName() {
		return this.name;
	}

	public String getDescription() {
		return this.description;
	}

	public Boolean getIsActive() {
		return this.isActive;
	}

	public Boolean getIsDefault() {
		return this.isDefault;
	}

	public Integer getSortOrder() {
		return this.sortOrder;
	}

	public Integer getTrialDays() {
		return this.trialDays;
	}

	public PlanType getPlanType() {
		return this.planType;
	}

	public PlanVisibility getVisibility() {
		return this.visibility;
	}

	public Integer getMaxUsers() {
		return this.maxUsers;
	}

	public Integer getMaxProjects() {
		return this.maxProjects;
	}

	public Integer getMaxStorageMb() {
		return this.maxStorageMb;
	}

	public String getMetadataJson() {
		return this.metadataJson;
	}

	public void setId(final Long id) {
		this.id = id;
	}

	public void setCode(final String code) {
		this.code = code;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public void setDescription(final String description) {
		this.description = description;
	}

	public void setIsActive(final Boolean isActive) {
		this.isActive = isActive;
	}

	public void setIsDefault(final Boolean isDefault) {
		this.isDefault = isDefault;
	}

	public void setSortOrder(final Integer sortOrder) {
		this.sortOrder = sortOrder;
	}

	public void setTrialDays(final Integer trialDays) {
		this.trialDays = trialDays;
	}

	public void setPlanType(final PlanType planType) {
		this.planType = planType;
	}

	public void setVisibility(final PlanVisibility visibility) {
		this.visibility = visibility;
	}

	public void setMaxUsers(final Integer maxUsers) {
		this.maxUsers = maxUsers;
	}

	public void setMaxProjects(final Integer maxProjects) {
		this.maxProjects = maxProjects;
	}

	public void setMaxStorageMb(final Integer maxStorageMb) {
		this.maxStorageMb = maxStorageMb;
	}

	public void setMetadataJson(final String metadataJson) {
		this.metadataJson = metadataJson;
	}
}
