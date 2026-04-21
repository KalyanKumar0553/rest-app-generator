package com.src.main.subscription.dto;

import java.util.List;
import com.src.main.subscription.enums.PlanType;
import com.src.main.subscription.enums.PlanVisibility;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

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
	private List<String> roleNames;
	private String metadataJson;

	public PlanRequest() {
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

	public List<String> getRoleNames() {
		return this.roleNames;
	}

	public String getMetadataJson() {
		return this.metadataJson;
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

	public void setRoleNames(final List<String> roleNames) {
		this.roleNames = roleNames;
	}

	public void setMetadataJson(final String metadataJson) {
		this.metadataJson = metadataJson;
	}

	@Override
	public boolean equals(final Object o) {
		if (o == this) return true;
		if (!(o instanceof PlanRequest)) return false;
		final PlanRequest other = (PlanRequest) o;
		if (!other.canEqual((Object) this)) return false;
		final Object this$isActive = this.getIsActive();
		final Object other$isActive = other.getIsActive();
		if (this$isActive == null ? other$isActive != null : !this$isActive.equals(other$isActive)) return false;
		final Object this$isDefault = this.getIsDefault();
		final Object other$isDefault = other.getIsDefault();
		if (this$isDefault == null ? other$isDefault != null : !this$isDefault.equals(other$isDefault)) return false;
		final Object this$sortOrder = this.getSortOrder();
		final Object other$sortOrder = other.getSortOrder();
		if (this$sortOrder == null ? other$sortOrder != null : !this$sortOrder.equals(other$sortOrder)) return false;
		final Object this$trialDays = this.getTrialDays();
		final Object other$trialDays = other.getTrialDays();
		if (this$trialDays == null ? other$trialDays != null : !this$trialDays.equals(other$trialDays)) return false;
		final Object this$maxUsers = this.getMaxUsers();
		final Object other$maxUsers = other.getMaxUsers();
		if (this$maxUsers == null ? other$maxUsers != null : !this$maxUsers.equals(other$maxUsers)) return false;
		final Object this$maxProjects = this.getMaxProjects();
		final Object other$maxProjects = other.getMaxProjects();
		if (this$maxProjects == null ? other$maxProjects != null : !this$maxProjects.equals(other$maxProjects)) return false;
		final Object this$maxStorageMb = this.getMaxStorageMb();
		final Object other$maxStorageMb = other.getMaxStorageMb();
		if (this$maxStorageMb == null ? other$maxStorageMb != null : !this$maxStorageMb.equals(other$maxStorageMb)) return false;
		final Object this$code = this.getCode();
		final Object other$code = other.getCode();
		if (this$code == null ? other$code != null : !this$code.equals(other$code)) return false;
		final Object this$name = this.getName();
		final Object other$name = other.getName();
		if (this$name == null ? other$name != null : !this$name.equals(other$name)) return false;
		final Object this$description = this.getDescription();
		final Object other$description = other.getDescription();
		if (this$description == null ? other$description != null : !this$description.equals(other$description)) return false;
		final Object this$planType = this.getPlanType();
		final Object other$planType = other.getPlanType();
		if (this$planType == null ? other$planType != null : !this$planType.equals(other$planType)) return false;
		final Object this$visibility = this.getVisibility();
		final Object other$visibility = other.getVisibility();
		if (this$visibility == null ? other$visibility != null : !this$visibility.equals(other$visibility)) return false;
		final Object this$roleNames = this.getRoleNames();
		final Object other$roleNames = other.getRoleNames();
		if (this$roleNames == null ? other$roleNames != null : !this$roleNames.equals(other$roleNames)) return false;
		final Object this$metadataJson = this.getMetadataJson();
		final Object other$metadataJson = other.getMetadataJson();
		if (this$metadataJson == null ? other$metadataJson != null : !this$metadataJson.equals(other$metadataJson)) return false;
		return true;
	}

	protected boolean canEqual(final Object other) {
		return other instanceof PlanRequest;
	}

	@Override
	public int hashCode() {
		final int PRIME = 59;
		int result = 1;
		final Object $isActive = this.getIsActive();
		result = result * PRIME + ($isActive == null ? 43 : $isActive.hashCode());
		final Object $isDefault = this.getIsDefault();
		result = result * PRIME + ($isDefault == null ? 43 : $isDefault.hashCode());
		final Object $sortOrder = this.getSortOrder();
		result = result * PRIME + ($sortOrder == null ? 43 : $sortOrder.hashCode());
		final Object $trialDays = this.getTrialDays();
		result = result * PRIME + ($trialDays == null ? 43 : $trialDays.hashCode());
		final Object $maxUsers = this.getMaxUsers();
		result = result * PRIME + ($maxUsers == null ? 43 : $maxUsers.hashCode());
		final Object $maxProjects = this.getMaxProjects();
		result = result * PRIME + ($maxProjects == null ? 43 : $maxProjects.hashCode());
		final Object $maxStorageMb = this.getMaxStorageMb();
		result = result * PRIME + ($maxStorageMb == null ? 43 : $maxStorageMb.hashCode());
		final Object $code = this.getCode();
		result = result * PRIME + ($code == null ? 43 : $code.hashCode());
		final Object $name = this.getName();
		result = result * PRIME + ($name == null ? 43 : $name.hashCode());
		final Object $description = this.getDescription();
		result = result * PRIME + ($description == null ? 43 : $description.hashCode());
		final Object $planType = this.getPlanType();
		result = result * PRIME + ($planType == null ? 43 : $planType.hashCode());
		final Object $visibility = this.getVisibility();
		result = result * PRIME + ($visibility == null ? 43 : $visibility.hashCode());
		final Object $roleNames = this.getRoleNames();
		result = result * PRIME + ($roleNames == null ? 43 : $roleNames.hashCode());
		final Object $metadataJson = this.getMetadataJson();
		result = result * PRIME + ($metadataJson == null ? 43 : $metadataJson.hashCode());
		return result;
	}

	@Override
	public String toString() {
		return "PlanRequest(code=" + this.getCode() + ", name=" + this.getName() + ", description=" + this.getDescription() + ", isActive=" + this.getIsActive() + ", isDefault=" + this.getIsDefault() + ", sortOrder=" + this.getSortOrder() + ", trialDays=" + this.getTrialDays() + ", planType=" + this.getPlanType() + ", visibility=" + this.getVisibility() + ", maxUsers=" + this.getMaxUsers() + ", maxProjects=" + this.getMaxProjects() + ", maxStorageMb=" + this.getMaxStorageMb() + ", roleNames=" + this.getRoleNames() + ", metadataJson=" + this.getMetadataJson() + ")";
	}
}
