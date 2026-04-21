package com.src.main.subscription.entity;

import java.math.BigDecimal;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "plan_feature_mapping")
public class PlanFeatureMappingEntity extends BaseSubscriptionEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "plan_id", nullable = false)
	private SubscriptionPlanEntity plan;
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "feature_id", nullable = false)
	private SubscriptionFeatureEntity feature;
	@Column(name = "is_enabled", nullable = false)
	private Boolean isEnabled = Boolean.FALSE;
	@Column(name = "limit_value")
	private Long limitValue;
	@Column(name = "decimal_value", precision = 19, scale = 4)
	private BigDecimal decimalValue;
	@Column(name = "string_value", length = 500)
	private String stringValue;
	@Column(name = "metadata_json", columnDefinition = "text")
	private String metadataJson;

	public Long getId() {
		return this.id;
	}

	public SubscriptionPlanEntity getPlan() {
		return this.plan;
	}

	public SubscriptionFeatureEntity getFeature() {
		return this.feature;
	}

	public Boolean getIsEnabled() {
		return this.isEnabled;
	}

	public Long getLimitValue() {
		return this.limitValue;
	}

	public BigDecimal getDecimalValue() {
		return this.decimalValue;
	}

	public String getStringValue() {
		return this.stringValue;
	}

	public String getMetadataJson() {
		return this.metadataJson;
	}

	public void setId(final Long id) {
		this.id = id;
	}

	public void setPlan(final SubscriptionPlanEntity plan) {
		this.plan = plan;
	}

	public void setFeature(final SubscriptionFeatureEntity feature) {
		this.feature = feature;
	}

	public void setIsEnabled(final Boolean isEnabled) {
		this.isEnabled = isEnabled;
	}

	public void setLimitValue(final Long limitValue) {
		this.limitValue = limitValue;
	}

	public void setDecimalValue(final BigDecimal decimalValue) {
		this.decimalValue = decimalValue;
	}

	public void setStringValue(final String stringValue) {
		this.stringValue = stringValue;
	}

	public void setMetadataJson(final String metadataJson) {
		this.metadataJson = metadataJson;
	}
}
