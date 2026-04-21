package com.src.main.subscription.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.src.main.subscription.enums.DiscountType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "subscription_coupon")
public class SubscriptionCouponEntity extends BaseSubscriptionEntity {
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
	@Enumerated(EnumType.STRING)
	@Column(name = "discount_type", nullable = false, length = 50)
	private DiscountType discountType;
	@Column(name = "discount_value", nullable = false, precision = 19, scale = 4)
	private BigDecimal discountValue;
	@Column(name = "currency_code", length = 10)
	private String currencyCode;
	@Column(name = "valid_from", nullable = false)
	private LocalDateTime validFrom;
	@Column(name = "valid_to")
	private LocalDateTime validTo;
	@Column(name = "max_redemptions")
	private Integer maxRedemptions;
	@Column(name = "max_redemptions_per_tenant")
	private Integer maxRedemptionsPerTenant;
	@Column(name = "first_subscription_only", nullable = false)
	private Boolean firstSubscriptionOnly = Boolean.FALSE;
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

	public DiscountType getDiscountType() {
		return this.discountType;
	}

	public BigDecimal getDiscountValue() {
		return this.discountValue;
	}

	public String getCurrencyCode() {
		return this.currencyCode;
	}

	public LocalDateTime getValidFrom() {
		return this.validFrom;
	}

	public LocalDateTime getValidTo() {
		return this.validTo;
	}

	public Integer getMaxRedemptions() {
		return this.maxRedemptions;
	}

	public Integer getMaxRedemptionsPerTenant() {
		return this.maxRedemptionsPerTenant;
	}

	public Boolean getFirstSubscriptionOnly() {
		return this.firstSubscriptionOnly;
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

	public void setDiscountType(final DiscountType discountType) {
		this.discountType = discountType;
	}

	public void setDiscountValue(final BigDecimal discountValue) {
		this.discountValue = discountValue;
	}

	public void setCurrencyCode(final String currencyCode) {
		this.currencyCode = currencyCode;
	}

	public void setValidFrom(final LocalDateTime validFrom) {
		this.validFrom = validFrom;
	}

	public void setValidTo(final LocalDateTime validTo) {
		this.validTo = validTo;
	}

	public void setMaxRedemptions(final Integer maxRedemptions) {
		this.maxRedemptions = maxRedemptions;
	}

	public void setMaxRedemptionsPerTenant(final Integer maxRedemptionsPerTenant) {
		this.maxRedemptionsPerTenant = maxRedemptionsPerTenant;
	}

	public void setFirstSubscriptionOnly(final Boolean firstSubscriptionOnly) {
		this.firstSubscriptionOnly = firstSubscriptionOnly;
	}

	public void setMetadataJson(final String metadataJson) {
		this.metadataJson = metadataJson;
	}
}
