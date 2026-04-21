package com.src.main.subscription.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.src.main.subscription.enums.OverrideType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "customer_feature_override")
public class CustomerFeatureOverrideEntity extends BaseSubscriptionEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Column(name = "tenant_id", nullable = false)
	private Long tenantId;
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "feature_id", nullable = false)
	private SubscriptionFeatureEntity feature;
	@Column(name = "is_enabled")
	private Boolean isEnabled;
	@Column(name = "limit_value")
	private Long limitValue;
	@Column(name = "decimal_value", precision = 19, scale = 4)
	private BigDecimal decimalValue;
	@Column(name = "string_value", length = 500)
	private String stringValue;
	@Enumerated(EnumType.STRING)
	@Column(name = "override_type", nullable = false, length = 50)
	private OverrideType overrideType;
	@Column(name = "reason", length = 500)
	private String reason;
	@Column(name = "effective_from", nullable = false)
	private LocalDateTime effectiveFrom;
	@Column(name = "effective_to")
	private LocalDateTime effectiveTo;
	@Column(name = "is_active", nullable = false)
	private Boolean isActive = Boolean.TRUE;
	@Column(name = "metadata_json", columnDefinition = "text")
	private String metadataJson;

	public Long getId() {
		return this.id;
	}

	public Long getTenantId() {
		return this.tenantId;
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

	public OverrideType getOverrideType() {
		return this.overrideType;
	}

	public String getReason() {
		return this.reason;
	}

	public LocalDateTime getEffectiveFrom() {
		return this.effectiveFrom;
	}

	public LocalDateTime getEffectiveTo() {
		return this.effectiveTo;
	}

	public Boolean getIsActive() {
		return this.isActive;
	}

	public String getMetadataJson() {
		return this.metadataJson;
	}

	public void setId(final Long id) {
		this.id = id;
	}

	public void setTenantId(final Long tenantId) {
		this.tenantId = tenantId;
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

	public void setOverrideType(final OverrideType overrideType) {
		this.overrideType = overrideType;
	}

	public void setReason(final String reason) {
		this.reason = reason;
	}

	public void setEffectiveFrom(final LocalDateTime effectiveFrom) {
		this.effectiveFrom = effectiveFrom;
	}

	public void setEffectiveTo(final LocalDateTime effectiveTo) {
		this.effectiveTo = effectiveTo;
	}

	public void setIsActive(final Boolean isActive) {
		this.isActive = isActive;
	}

	public void setMetadataJson(final String metadataJson) {
		this.metadataJson = metadataJson;
	}
}
