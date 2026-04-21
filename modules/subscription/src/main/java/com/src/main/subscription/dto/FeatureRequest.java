package com.src.main.subscription.dto;

import com.src.main.subscription.enums.FeatureType;
import com.src.main.subscription.enums.ResetPolicy;
import com.src.main.subscription.enums.ValueDataType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

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

	public FeatureRequest() {
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

	public FeatureType getFeatureType() {
		return this.featureType;
	}

	public ValueDataType getValueDataType() {
		return this.valueDataType;
	}

	public String getUnit() {
		return this.unit;
	}

	public ResetPolicy getResetPolicy() {
		return this.resetPolicy;
	}

	public Boolean getIsActive() {
		return this.isActive;
	}

	public Boolean getIsSystem() {
		return this.isSystem;
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

	public void setFeatureType(final FeatureType featureType) {
		this.featureType = featureType;
	}

	public void setValueDataType(final ValueDataType valueDataType) {
		this.valueDataType = valueDataType;
	}

	public void setUnit(final String unit) {
		this.unit = unit;
	}

	public void setResetPolicy(final ResetPolicy resetPolicy) {
		this.resetPolicy = resetPolicy;
	}

	public void setIsActive(final Boolean isActive) {
		this.isActive = isActive;
	}

	public void setIsSystem(final Boolean isSystem) {
		this.isSystem = isSystem;
	}

	public void setMetadataJson(final String metadataJson) {
		this.metadataJson = metadataJson;
	}

	@Override
	public boolean equals(final Object o) {
		if (o == this) return true;
		if (!(o instanceof FeatureRequest)) return false;
		final FeatureRequest other = (FeatureRequest) o;
		if (!other.canEqual((Object) this)) return false;
		final Object this$isActive = this.getIsActive();
		final Object other$isActive = other.getIsActive();
		if (this$isActive == null ? other$isActive != null : !this$isActive.equals(other$isActive)) return false;
		final Object this$isSystem = this.getIsSystem();
		final Object other$isSystem = other.getIsSystem();
		if (this$isSystem == null ? other$isSystem != null : !this$isSystem.equals(other$isSystem)) return false;
		final Object this$code = this.getCode();
		final Object other$code = other.getCode();
		if (this$code == null ? other$code != null : !this$code.equals(other$code)) return false;
		final Object this$name = this.getName();
		final Object other$name = other.getName();
		if (this$name == null ? other$name != null : !this$name.equals(other$name)) return false;
		final Object this$description = this.getDescription();
		final Object other$description = other.getDescription();
		if (this$description == null ? other$description != null : !this$description.equals(other$description)) return false;
		final Object this$featureType = this.getFeatureType();
		final Object other$featureType = other.getFeatureType();
		if (this$featureType == null ? other$featureType != null : !this$featureType.equals(other$featureType)) return false;
		final Object this$valueDataType = this.getValueDataType();
		final Object other$valueDataType = other.getValueDataType();
		if (this$valueDataType == null ? other$valueDataType != null : !this$valueDataType.equals(other$valueDataType)) return false;
		final Object this$unit = this.getUnit();
		final Object other$unit = other.getUnit();
		if (this$unit == null ? other$unit != null : !this$unit.equals(other$unit)) return false;
		final Object this$resetPolicy = this.getResetPolicy();
		final Object other$resetPolicy = other.getResetPolicy();
		if (this$resetPolicy == null ? other$resetPolicy != null : !this$resetPolicy.equals(other$resetPolicy)) return false;
		final Object this$metadataJson = this.getMetadataJson();
		final Object other$metadataJson = other.getMetadataJson();
		if (this$metadataJson == null ? other$metadataJson != null : !this$metadataJson.equals(other$metadataJson)) return false;
		return true;
	}

	protected boolean canEqual(final Object other) {
		return other instanceof FeatureRequest;
	}

	@Override
	public int hashCode() {
		final int PRIME = 59;
		int result = 1;
		final Object $isActive = this.getIsActive();
		result = result * PRIME + ($isActive == null ? 43 : $isActive.hashCode());
		final Object $isSystem = this.getIsSystem();
		result = result * PRIME + ($isSystem == null ? 43 : $isSystem.hashCode());
		final Object $code = this.getCode();
		result = result * PRIME + ($code == null ? 43 : $code.hashCode());
		final Object $name = this.getName();
		result = result * PRIME + ($name == null ? 43 : $name.hashCode());
		final Object $description = this.getDescription();
		result = result * PRIME + ($description == null ? 43 : $description.hashCode());
		final Object $featureType = this.getFeatureType();
		result = result * PRIME + ($featureType == null ? 43 : $featureType.hashCode());
		final Object $valueDataType = this.getValueDataType();
		result = result * PRIME + ($valueDataType == null ? 43 : $valueDataType.hashCode());
		final Object $unit = this.getUnit();
		result = result * PRIME + ($unit == null ? 43 : $unit.hashCode());
		final Object $resetPolicy = this.getResetPolicy();
		result = result * PRIME + ($resetPolicy == null ? 43 : $resetPolicy.hashCode());
		final Object $metadataJson = this.getMetadataJson();
		result = result * PRIME + ($metadataJson == null ? 43 : $metadataJson.hashCode());
		return result;
	}

	@Override
	public String toString() {
		return "FeatureRequest(code=" + this.getCode() + ", name=" + this.getName() + ", description=" + this.getDescription() + ", featureType=" + this.getFeatureType() + ", valueDataType=" + this.getValueDataType() + ", unit=" + this.getUnit() + ", resetPolicy=" + this.getResetPolicy() + ", isActive=" + this.getIsActive() + ", isSystem=" + this.getIsSystem() + ", metadataJson=" + this.getMetadataJson() + ")";
	}
}
