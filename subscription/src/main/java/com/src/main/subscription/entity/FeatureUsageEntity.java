package com.src.main.subscription.entity;

import java.time.LocalDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "feature_usage")
public class FeatureUsageEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Column(name = "tenant_id", nullable = false)
	private Long tenantId;
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "feature_id", nullable = false)
	private SubscriptionFeatureEntity feature;
	@Column(name = "period_key", nullable = false, length = 50)
	private String periodKey;
	@Column(name = "period_start", nullable = false)
	private LocalDateTime periodStart;
	@Column(name = "period_end", nullable = false)
	private LocalDateTime periodEnd;
	@Column(name = "used_value", nullable = false)
	private Long usedValue = 0L;
	@Column(name = "reserved_value", nullable = false)
	private Long reservedValue = 0L;
	@Column(name = "last_consumed_at")
	private LocalDateTime lastConsumedAt;
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;
	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;

	@PrePersist
	void onCreate() {
		LocalDateTime now = LocalDateTime.now();
		createdAt = now;
		updatedAt = now;
	}

	@PreUpdate
	void onUpdate() {
		updatedAt = LocalDateTime.now();
	}

	public Long getId() {
		return this.id;
	}

	public Long getTenantId() {
		return this.tenantId;
	}

	public SubscriptionFeatureEntity getFeature() {
		return this.feature;
	}

	public String getPeriodKey() {
		return this.periodKey;
	}

	public LocalDateTime getPeriodStart() {
		return this.periodStart;
	}

	public LocalDateTime getPeriodEnd() {
		return this.periodEnd;
	}

	public Long getUsedValue() {
		return this.usedValue;
	}

	public Long getReservedValue() {
		return this.reservedValue;
	}

	public LocalDateTime getLastConsumedAt() {
		return this.lastConsumedAt;
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

	public void setTenantId(final Long tenantId) {
		this.tenantId = tenantId;
	}

	public void setFeature(final SubscriptionFeatureEntity feature) {
		this.feature = feature;
	}

	public void setPeriodKey(final String periodKey) {
		this.periodKey = periodKey;
	}

	public void setPeriodStart(final LocalDateTime periodStart) {
		this.periodStart = periodStart;
	}

	public void setPeriodEnd(final LocalDateTime periodEnd) {
		this.periodEnd = periodEnd;
	}

	public void setUsedValue(final Long usedValue) {
		this.usedValue = usedValue;
	}

	public void setReservedValue(final Long reservedValue) {
		this.reservedValue = reservedValue;
	}

	public void setLastConsumedAt(final LocalDateTime lastConsumedAt) {
		this.lastConsumedAt = lastConsumedAt;
	}

	public void setCreatedAt(final LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public void setUpdatedAt(final LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}
}
