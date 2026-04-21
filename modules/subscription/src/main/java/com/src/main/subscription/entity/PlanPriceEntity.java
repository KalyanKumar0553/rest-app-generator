package com.src.main.subscription.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.src.main.subscription.enums.BillingCycle;
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
@Table(name = "plan_price")
public class PlanPriceEntity extends BaseSubscriptionEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "plan_id", nullable = false)
	private SubscriptionPlanEntity plan;
	@Enumerated(EnumType.STRING)
	@Column(name = "billing_cycle", nullable = false, length = 50)
	private BillingCycle billingCycle;
	@Column(name = "currency_code", nullable = false, length = 10)
	private String currencyCode;
	@Column(name = "amount", nullable = false, precision = 19, scale = 2)
	private BigDecimal amount;
	@Column(name = "discount_percent", precision = 5, scale = 2)
	private BigDecimal discountPercent;
	@Column(name = "effective_from", nullable = false)
	private LocalDateTime effectiveFrom;
	@Column(name = "effective_to")
	private LocalDateTime effectiveTo;
	@Column(name = "is_active", nullable = false)
	private Boolean isActive = Boolean.TRUE;
	@Column(name = "display_label", length = 100)
	private String displayLabel;
	@Column(name = "metadata_json", columnDefinition = "text")
	private String metadataJson;

	public Long getId() {
		return this.id;
	}

	public SubscriptionPlanEntity getPlan() {
		return this.plan;
	}

	public BillingCycle getBillingCycle() {
		return this.billingCycle;
	}

	public String getCurrencyCode() {
		return this.currencyCode;
	}

	public BigDecimal getAmount() {
		return this.amount;
	}

	public BigDecimal getDiscountPercent() {
		return this.discountPercent;
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

	public String getDisplayLabel() {
		return this.displayLabel;
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

	public void setBillingCycle(final BillingCycle billingCycle) {
		this.billingCycle = billingCycle;
	}

	public void setCurrencyCode(final String currencyCode) {
		this.currencyCode = currencyCode;
	}

	public void setAmount(final BigDecimal amount) {
		this.amount = amount;
	}

	public void setDiscountPercent(final BigDecimal discountPercent) {
		this.discountPercent = discountPercent;
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

	public void setDisplayLabel(final String displayLabel) {
		this.displayLabel = displayLabel;
	}

	public void setMetadataJson(final String metadataJson) {
		this.metadataJson = metadataJson;
	}
}
