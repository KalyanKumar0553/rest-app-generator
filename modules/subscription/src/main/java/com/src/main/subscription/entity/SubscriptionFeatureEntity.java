package com.src.main.subscription.entity;

import com.src.main.subscription.enums.FeatureType;
import com.src.main.subscription.enums.ResetPolicy;
import com.src.main.subscription.enums.ValueDataType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "subscription_feature")
public class SubscriptionFeatureEntity extends BaseSubscriptionEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Column(name = "code", nullable = false, length = 100, unique = true)
	private String code;
	@Column(name = "name", nullable = false, length = 150)
	private String name;
	@Column(name = "description", length = 1000)
	private String description;
	@Enumerated(EnumType.STRING)
	@Column(name = "feature_type", nullable = false, length = 50)
	private FeatureType featureType;
	@Enumerated(EnumType.STRING)
	@Column(name = "value_data_type", length = 50)
	private ValueDataType valueDataType;
	@Column(name = "unit", length = 50)
	private String unit;
	@Enumerated(EnumType.STRING)
	@Column(name = "reset_policy", length = 50)
	private ResetPolicy resetPolicy;
	@Column(name = "is_active", nullable = false)
	private Boolean isActive = Boolean.TRUE;
	@Column(name = "is_system", nullable = false)
	private Boolean isSystem = Boolean.FALSE;
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
}
