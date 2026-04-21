package com.src.main.subscription.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import com.src.main.subscription.enums.DiscountType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class SubscriptionCouponRequest {
	@NotBlank
	private String code;
	@NotBlank
	private String name;
	private String description;
	private Boolean isActive = Boolean.TRUE;
	@NotNull
	private DiscountType discountType;
	@NotNull
	private BigDecimal discountValue;
	private String currencyCode;
	@NotNull
	private LocalDateTime validFrom;
	private LocalDateTime validTo;
	private Integer maxRedemptions;
	private Integer maxRedemptionsPerTenant;
	private Boolean firstSubscriptionOnly = Boolean.FALSE;
	private List<Long> applicablePlanIds;
	private String metadataJson;

	public SubscriptionCouponRequest() {
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

	public List<Long> getApplicablePlanIds() {
		return this.applicablePlanIds;
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

	public void setApplicablePlanIds(final List<Long> applicablePlanIds) {
		this.applicablePlanIds = applicablePlanIds;
	}

	public void setMetadataJson(final String metadataJson) {
		this.metadataJson = metadataJson;
	}

	@Override
	public boolean equals(final Object o) {
		if (o == this) return true;
		if (!(o instanceof SubscriptionCouponRequest)) return false;
		final SubscriptionCouponRequest other = (SubscriptionCouponRequest) o;
		if (!other.canEqual((Object) this)) return false;
		final Object this$isActive = this.getIsActive();
		final Object other$isActive = other.getIsActive();
		if (this$isActive == null ? other$isActive != null : !this$isActive.equals(other$isActive)) return false;
		final Object this$maxRedemptions = this.getMaxRedemptions();
		final Object other$maxRedemptions = other.getMaxRedemptions();
		if (this$maxRedemptions == null ? other$maxRedemptions != null : !this$maxRedemptions.equals(other$maxRedemptions)) return false;
		final Object this$maxRedemptionsPerTenant = this.getMaxRedemptionsPerTenant();
		final Object other$maxRedemptionsPerTenant = other.getMaxRedemptionsPerTenant();
		if (this$maxRedemptionsPerTenant == null ? other$maxRedemptionsPerTenant != null : !this$maxRedemptionsPerTenant.equals(other$maxRedemptionsPerTenant)) return false;
		final Object this$firstSubscriptionOnly = this.getFirstSubscriptionOnly();
		final Object other$firstSubscriptionOnly = other.getFirstSubscriptionOnly();
		if (this$firstSubscriptionOnly == null ? other$firstSubscriptionOnly != null : !this$firstSubscriptionOnly.equals(other$firstSubscriptionOnly)) return false;
		final Object this$code = this.getCode();
		final Object other$code = other.getCode();
		if (this$code == null ? other$code != null : !this$code.equals(other$code)) return false;
		final Object this$name = this.getName();
		final Object other$name = other.getName();
		if (this$name == null ? other$name != null : !this$name.equals(other$name)) return false;
		final Object this$description = this.getDescription();
		final Object other$description = other.getDescription();
		if (this$description == null ? other$description != null : !this$description.equals(other$description)) return false;
		final Object this$discountType = this.getDiscountType();
		final Object other$discountType = other.getDiscountType();
		if (this$discountType == null ? other$discountType != null : !this$discountType.equals(other$discountType)) return false;
		final Object this$discountValue = this.getDiscountValue();
		final Object other$discountValue = other.getDiscountValue();
		if (this$discountValue == null ? other$discountValue != null : !this$discountValue.equals(other$discountValue)) return false;
		final Object this$currencyCode = this.getCurrencyCode();
		final Object other$currencyCode = other.getCurrencyCode();
		if (this$currencyCode == null ? other$currencyCode != null : !this$currencyCode.equals(other$currencyCode)) return false;
		final Object this$validFrom = this.getValidFrom();
		final Object other$validFrom = other.getValidFrom();
		if (this$validFrom == null ? other$validFrom != null : !this$validFrom.equals(other$validFrom)) return false;
		final Object this$validTo = this.getValidTo();
		final Object other$validTo = other.getValidTo();
		if (this$validTo == null ? other$validTo != null : !this$validTo.equals(other$validTo)) return false;
		final Object this$applicablePlanIds = this.getApplicablePlanIds();
		final Object other$applicablePlanIds = other.getApplicablePlanIds();
		if (this$applicablePlanIds == null ? other$applicablePlanIds != null : !this$applicablePlanIds.equals(other$applicablePlanIds)) return false;
		final Object this$metadataJson = this.getMetadataJson();
		final Object other$metadataJson = other.getMetadataJson();
		if (this$metadataJson == null ? other$metadataJson != null : !this$metadataJson.equals(other$metadataJson)) return false;
		return true;
	}

	protected boolean canEqual(final Object other) {
		return other instanceof SubscriptionCouponRequest;
	}

	@Override
	public int hashCode() {
		final int PRIME = 59;
		int result = 1;
		final Object $isActive = this.getIsActive();
		result = result * PRIME + ($isActive == null ? 43 : $isActive.hashCode());
		final Object $maxRedemptions = this.getMaxRedemptions();
		result = result * PRIME + ($maxRedemptions == null ? 43 : $maxRedemptions.hashCode());
		final Object $maxRedemptionsPerTenant = this.getMaxRedemptionsPerTenant();
		result = result * PRIME + ($maxRedemptionsPerTenant == null ? 43 : $maxRedemptionsPerTenant.hashCode());
		final Object $firstSubscriptionOnly = this.getFirstSubscriptionOnly();
		result = result * PRIME + ($firstSubscriptionOnly == null ? 43 : $firstSubscriptionOnly.hashCode());
		final Object $code = this.getCode();
		result = result * PRIME + ($code == null ? 43 : $code.hashCode());
		final Object $name = this.getName();
		result = result * PRIME + ($name == null ? 43 : $name.hashCode());
		final Object $description = this.getDescription();
		result = result * PRIME + ($description == null ? 43 : $description.hashCode());
		final Object $discountType = this.getDiscountType();
		result = result * PRIME + ($discountType == null ? 43 : $discountType.hashCode());
		final Object $discountValue = this.getDiscountValue();
		result = result * PRIME + ($discountValue == null ? 43 : $discountValue.hashCode());
		final Object $currencyCode = this.getCurrencyCode();
		result = result * PRIME + ($currencyCode == null ? 43 : $currencyCode.hashCode());
		final Object $validFrom = this.getValidFrom();
		result = result * PRIME + ($validFrom == null ? 43 : $validFrom.hashCode());
		final Object $validTo = this.getValidTo();
		result = result * PRIME + ($validTo == null ? 43 : $validTo.hashCode());
		final Object $applicablePlanIds = this.getApplicablePlanIds();
		result = result * PRIME + ($applicablePlanIds == null ? 43 : $applicablePlanIds.hashCode());
		final Object $metadataJson = this.getMetadataJson();
		result = result * PRIME + ($metadataJson == null ? 43 : $metadataJson.hashCode());
		return result;
	}

	@Override
	public String toString() {
		return "SubscriptionCouponRequest(code=" + this.getCode() + ", name=" + this.getName() + ", description=" + this.getDescription() + ", isActive=" + this.getIsActive() + ", discountType=" + this.getDiscountType() + ", discountValue=" + this.getDiscountValue() + ", currencyCode=" + this.getCurrencyCode() + ", validFrom=" + this.getValidFrom() + ", validTo=" + this.getValidTo() + ", maxRedemptions=" + this.getMaxRedemptions() + ", maxRedemptionsPerTenant=" + this.getMaxRedemptionsPerTenant() + ", firstSubscriptionOnly=" + this.getFirstSubscriptionOnly() + ", applicablePlanIds=" + this.getApplicablePlanIds() + ", metadataJson=" + this.getMetadataJson() + ")";
	}
}
