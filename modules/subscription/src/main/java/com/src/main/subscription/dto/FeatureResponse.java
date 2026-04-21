package com.src.main.subscription.dto;

import java.time.LocalDateTime;
import com.src.main.subscription.enums.FeatureType;
import com.src.main.subscription.enums.ResetPolicy;
import com.src.main.subscription.enums.ValueDataType;

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

	FeatureResponse(final Long id, final String code, final String name, final String description, final FeatureType featureType, final ValueDataType valueDataType, final String unit, final ResetPolicy resetPolicy, final Boolean isActive, final Boolean isSystem, final String metadataJson, final LocalDateTime createdAt, final LocalDateTime updatedAt) {
		this.id = id;
		this.code = code;
		this.name = name;
		this.description = description;
		this.featureType = featureType;
		this.valueDataType = valueDataType;
		this.unit = unit;
		this.resetPolicy = resetPolicy;
		this.isActive = isActive;
		this.isSystem = isSystem;
		this.metadataJson = metadataJson;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}


	public static class FeatureResponseBuilder {
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

		FeatureResponseBuilder() {
		}

		/**
		 * @return {@code this}.
		 */
		public FeatureResponse.FeatureResponseBuilder id(final Long id) {
			this.id = id;
			return this;
		}

		/**
		 * @return {@code this}.
		 */
		public FeatureResponse.FeatureResponseBuilder code(final String code) {
			this.code = code;
			return this;
		}

		/**
		 * @return {@code this}.
		 */
		public FeatureResponse.FeatureResponseBuilder name(final String name) {
			this.name = name;
			return this;
		}

		/**
		 * @return {@code this}.
		 */
		public FeatureResponse.FeatureResponseBuilder description(final String description) {
			this.description = description;
			return this;
		}

		/**
		 * @return {@code this}.
		 */
		public FeatureResponse.FeatureResponseBuilder featureType(final FeatureType featureType) {
			this.featureType = featureType;
			return this;
		}

		/**
		 * @return {@code this}.
		 */
		public FeatureResponse.FeatureResponseBuilder valueDataType(final ValueDataType valueDataType) {
			this.valueDataType = valueDataType;
			return this;
		}

		/**
		 * @return {@code this}.
		 */
		public FeatureResponse.FeatureResponseBuilder unit(final String unit) {
			this.unit = unit;
			return this;
		}

		/**
		 * @return {@code this}.
		 */
		public FeatureResponse.FeatureResponseBuilder resetPolicy(final ResetPolicy resetPolicy) {
			this.resetPolicy = resetPolicy;
			return this;
		}

		/**
		 * @return {@code this}.
		 */
		public FeatureResponse.FeatureResponseBuilder isActive(final Boolean isActive) {
			this.isActive = isActive;
			return this;
		}

		/**
		 * @return {@code this}.
		 */
		public FeatureResponse.FeatureResponseBuilder isSystem(final Boolean isSystem) {
			this.isSystem = isSystem;
			return this;
		}

		/**
		 * @return {@code this}.
		 */
		public FeatureResponse.FeatureResponseBuilder metadataJson(final String metadataJson) {
			this.metadataJson = metadataJson;
			return this;
		}

		/**
		 * @return {@code this}.
		 */
		public FeatureResponse.FeatureResponseBuilder createdAt(final LocalDateTime createdAt) {
			this.createdAt = createdAt;
			return this;
		}

		/**
		 * @return {@code this}.
		 */
		public FeatureResponse.FeatureResponseBuilder updatedAt(final LocalDateTime updatedAt) {
			this.updatedAt = updatedAt;
			return this;
		}

		public FeatureResponse build() {
			return new FeatureResponse(this.id, this.code, this.name, this.description, this.featureType, this.valueDataType, this.unit, this.resetPolicy, this.isActive, this.isSystem, this.metadataJson, this.createdAt, this.updatedAt);
		}

		@Override
		public String toString() {
			return "FeatureResponse.FeatureResponseBuilder(id=" + this.id + ", code=" + this.code + ", name=" + this.name + ", description=" + this.description + ", featureType=" + this.featureType + ", valueDataType=" + this.valueDataType + ", unit=" + this.unit + ", resetPolicy=" + this.resetPolicy + ", isActive=" + this.isActive + ", isSystem=" + this.isSystem + ", metadataJson=" + this.metadataJson + ", createdAt=" + this.createdAt + ", updatedAt=" + this.updatedAt + ")";
		}
	}

	public static FeatureResponse.FeatureResponseBuilder builder() {
		return new FeatureResponse.FeatureResponseBuilder();
	}

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

	public LocalDateTime getCreatedAt() {
		return this.createdAt;
	}

	public LocalDateTime getUpdatedAt() {
		return this.updatedAt;
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

	public void setCreatedAt(final LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public void setUpdatedAt(final LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}

	@Override
	public boolean equals(final Object o) {
		if (o == this) return true;
		if (!(o instanceof FeatureResponse)) return false;
		final FeatureResponse other = (FeatureResponse) o;
		if (!other.canEqual((Object) this)) return false;
		final Object this$id = this.getId();
		final Object other$id = other.getId();
		if (this$id == null ? other$id != null : !this$id.equals(other$id)) return false;
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
		final Object this$createdAt = this.getCreatedAt();
		final Object other$createdAt = other.getCreatedAt();
		if (this$createdAt == null ? other$createdAt != null : !this$createdAt.equals(other$createdAt)) return false;
		final Object this$updatedAt = this.getUpdatedAt();
		final Object other$updatedAt = other.getUpdatedAt();
		if (this$updatedAt == null ? other$updatedAt != null : !this$updatedAt.equals(other$updatedAt)) return false;
		return true;
	}

	protected boolean canEqual(final Object other) {
		return other instanceof FeatureResponse;
	}

	@Override
	public int hashCode() {
		final int PRIME = 59;
		int result = 1;
		final Object $id = this.getId();
		result = result * PRIME + ($id == null ? 43 : $id.hashCode());
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
		final Object $createdAt = this.getCreatedAt();
		result = result * PRIME + ($createdAt == null ? 43 : $createdAt.hashCode());
		final Object $updatedAt = this.getUpdatedAt();
		result = result * PRIME + ($updatedAt == null ? 43 : $updatedAt.hashCode());
		return result;
	}

	@Override
	public String toString() {
		return "FeatureResponse(id=" + this.getId() + ", code=" + this.getCode() + ", name=" + this.getName() + ", description=" + this.getDescription() + ", featureType=" + this.getFeatureType() + ", valueDataType=" + this.getValueDataType() + ", unit=" + this.getUnit() + ", resetPolicy=" + this.getResetPolicy() + ", isActive=" + this.getIsActive() + ", isSystem=" + this.getIsSystem() + ", metadataJson=" + this.getMetadataJson() + ", createdAt=" + this.getCreatedAt() + ", updatedAt=" + this.getUpdatedAt() + ")";
	}
}
