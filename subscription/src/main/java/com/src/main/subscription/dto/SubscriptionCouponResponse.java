package com.src.main.subscription.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import com.src.main.subscription.enums.DiscountType;

public class SubscriptionCouponResponse {
	private Long id;
	private String code;
	private String name;
	private String description;
	private Boolean isActive;
	private DiscountType discountType;
	private BigDecimal discountValue;
	private String currencyCode;
	private LocalDateTime validFrom;
	private LocalDateTime validTo;
	private Integer maxRedemptions;
	private Integer maxRedemptionsPerTenant;
	private Boolean firstSubscriptionOnly;
	private List<Long> applicablePlanIds;
	private String metadataJson;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;

	SubscriptionCouponResponse(final Long id, final String code, final String name, final String description, final Boolean isActive, final DiscountType discountType, final BigDecimal discountValue, final String currencyCode, final LocalDateTime validFrom, final LocalDateTime validTo, final Integer maxRedemptions, final Integer maxRedemptionsPerTenant, final Boolean firstSubscriptionOnly, final List<Long> applicablePlanIds, final String metadataJson, final LocalDateTime createdAt, final LocalDateTime updatedAt) {
		this.id = id;
		this.code = code;
		this.name = name;
		this.description = description;
		this.isActive = isActive;
		this.discountType = discountType;
		this.discountValue = discountValue;
		this.currencyCode = currencyCode;
		this.validFrom = validFrom;
		this.validTo = validTo;
		this.maxRedemptions = maxRedemptions;
		this.maxRedemptionsPerTenant = maxRedemptionsPerTenant;
		this.firstSubscriptionOnly = firstSubscriptionOnly;
		this.applicablePlanIds = applicablePlanIds;
		this.metadataJson = metadataJson;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}


	public static class SubscriptionCouponResponseBuilder {
		private Long id;
		private String code;
		private String name;
		private String description;
		private Boolean isActive;
		private DiscountType discountType;
		private BigDecimal discountValue;
		private String currencyCode;
		private LocalDateTime validFrom;
		private LocalDateTime validTo;
		private Integer maxRedemptions;
		private Integer maxRedemptionsPerTenant;
		private Boolean firstSubscriptionOnly;
		private List<Long> applicablePlanIds;
		private String metadataJson;
		private LocalDateTime createdAt;
		private LocalDateTime updatedAt;

		SubscriptionCouponResponseBuilder() {
		}

		/**
		 * @return {@code this}.
		 */
		public SubscriptionCouponResponse.SubscriptionCouponResponseBuilder id(final Long id) {
			this.id = id;
			return this;
		}

		/**
		 * @return {@code this}.
		 */
		public SubscriptionCouponResponse.SubscriptionCouponResponseBuilder code(final String code) {
			this.code = code;
			return this;
		}

		/**
		 * @return {@code this}.
		 */
		public SubscriptionCouponResponse.SubscriptionCouponResponseBuilder name(final String name) {
			this.name = name;
			return this;
		}

		/**
		 * @return {@code this}.
		 */
		public SubscriptionCouponResponse.SubscriptionCouponResponseBuilder description(final String description) {
			this.description = description;
			return this;
		}

		/**
		 * @return {@code this}.
		 */
		public SubscriptionCouponResponse.SubscriptionCouponResponseBuilder isActive(final Boolean isActive) {
			this.isActive = isActive;
			return this;
		}

		/**
		 * @return {@code this}.
		 */
		public SubscriptionCouponResponse.SubscriptionCouponResponseBuilder discountType(final DiscountType discountType) {
			this.discountType = discountType;
			return this;
		}

		/**
		 * @return {@code this}.
		 */
		public SubscriptionCouponResponse.SubscriptionCouponResponseBuilder discountValue(final BigDecimal discountValue) {
			this.discountValue = discountValue;
			return this;
		}

		/**
		 * @return {@code this}.
		 */
		public SubscriptionCouponResponse.SubscriptionCouponResponseBuilder currencyCode(final String currencyCode) {
			this.currencyCode = currencyCode;
			return this;
		}

		/**
		 * @return {@code this}.
		 */
		public SubscriptionCouponResponse.SubscriptionCouponResponseBuilder validFrom(final LocalDateTime validFrom) {
			this.validFrom = validFrom;
			return this;
		}

		/**
		 * @return {@code this}.
		 */
		public SubscriptionCouponResponse.SubscriptionCouponResponseBuilder validTo(final LocalDateTime validTo) {
			this.validTo = validTo;
			return this;
		}

		/**
		 * @return {@code this}.
		 */
		public SubscriptionCouponResponse.SubscriptionCouponResponseBuilder maxRedemptions(final Integer maxRedemptions) {
			this.maxRedemptions = maxRedemptions;
			return this;
		}

		/**
		 * @return {@code this}.
		 */
		public SubscriptionCouponResponse.SubscriptionCouponResponseBuilder maxRedemptionsPerTenant(final Integer maxRedemptionsPerTenant) {
			this.maxRedemptionsPerTenant = maxRedemptionsPerTenant;
			return this;
		}

		/**
		 * @return {@code this}.
		 */
		public SubscriptionCouponResponse.SubscriptionCouponResponseBuilder firstSubscriptionOnly(final Boolean firstSubscriptionOnly) {
			this.firstSubscriptionOnly = firstSubscriptionOnly;
			return this;
		}

		/**
		 * @return {@code this}.
		 */
		public SubscriptionCouponResponse.SubscriptionCouponResponseBuilder applicablePlanIds(final List<Long> applicablePlanIds) {
			this.applicablePlanIds = applicablePlanIds;
			return this;
		}

		/**
		 * @return {@code this}.
		 */
		public SubscriptionCouponResponse.SubscriptionCouponResponseBuilder metadataJson(final String metadataJson) {
			this.metadataJson = metadataJson;
			return this;
		}

		/**
		 * @return {@code this}.
		 */
		public SubscriptionCouponResponse.SubscriptionCouponResponseBuilder createdAt(final LocalDateTime createdAt) {
			this.createdAt = createdAt;
			return this;
		}

		/**
		 * @return {@code this}.
		 */
		public SubscriptionCouponResponse.SubscriptionCouponResponseBuilder updatedAt(final LocalDateTime updatedAt) {
			this.updatedAt = updatedAt;
			return this;
		}

		public SubscriptionCouponResponse build() {
			return new SubscriptionCouponResponse(this.id, this.code, this.name, this.description, this.isActive, this.discountType, this.discountValue, this.currencyCode, this.validFrom, this.validTo, this.maxRedemptions, this.maxRedemptionsPerTenant, this.firstSubscriptionOnly, this.applicablePlanIds, this.metadataJson, this.createdAt, this.updatedAt);
		}

		@Override
		public String toString() {
			return "SubscriptionCouponResponse.SubscriptionCouponResponseBuilder(id=" + this.id + ", code=" + this.code + ", name=" + this.name + ", description=" + this.description + ", isActive=" + this.isActive + ", discountType=" + this.discountType + ", discountValue=" + this.discountValue + ", currencyCode=" + this.currencyCode + ", validFrom=" + this.validFrom + ", validTo=" + this.validTo + ", maxRedemptions=" + this.maxRedemptions + ", maxRedemptionsPerTenant=" + this.maxRedemptionsPerTenant + ", firstSubscriptionOnly=" + this.firstSubscriptionOnly + ", applicablePlanIds=" + this.applicablePlanIds + ", metadataJson=" + this.metadataJson + ", createdAt=" + this.createdAt + ", updatedAt=" + this.updatedAt + ")";
		}
	}

	public static SubscriptionCouponResponse.SubscriptionCouponResponseBuilder builder() {
		return new SubscriptionCouponResponse.SubscriptionCouponResponseBuilder();
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

	public void setCreatedAt(final LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public void setUpdatedAt(final LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}

	@Override
	public boolean equals(final Object o) {
		if (o == this) return true;
		if (!(o instanceof SubscriptionCouponResponse)) return false;
		final SubscriptionCouponResponse other = (SubscriptionCouponResponse) o;
		if (!other.canEqual((Object) this)) return false;
		final Object this$id = this.getId();
		final Object other$id = other.getId();
		if (this$id == null ? other$id != null : !this$id.equals(other$id)) return false;
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
		final Object this$createdAt = this.getCreatedAt();
		final Object other$createdAt = other.getCreatedAt();
		if (this$createdAt == null ? other$createdAt != null : !this$createdAt.equals(other$createdAt)) return false;
		final Object this$updatedAt = this.getUpdatedAt();
		final Object other$updatedAt = other.getUpdatedAt();
		if (this$updatedAt == null ? other$updatedAt != null : !this$updatedAt.equals(other$updatedAt)) return false;
		return true;
	}

	protected boolean canEqual(final Object other) {
		return other instanceof SubscriptionCouponResponse;
	}

	@Override
	public int hashCode() {
		final int PRIME = 59;
		int result = 1;
		final Object $id = this.getId();
		result = result * PRIME + ($id == null ? 43 : $id.hashCode());
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
		final Object $createdAt = this.getCreatedAt();
		result = result * PRIME + ($createdAt == null ? 43 : $createdAt.hashCode());
		final Object $updatedAt = this.getUpdatedAt();
		result = result * PRIME + ($updatedAt == null ? 43 : $updatedAt.hashCode());
		return result;
	}

	@Override
	public String toString() {
		return "SubscriptionCouponResponse(id=" + this.getId() + ", code=" + this.getCode() + ", name=" + this.getName() + ", description=" + this.getDescription() + ", isActive=" + this.getIsActive() + ", discountType=" + this.getDiscountType() + ", discountValue=" + this.getDiscountValue() + ", currencyCode=" + this.getCurrencyCode() + ", validFrom=" + this.getValidFrom() + ", validTo=" + this.getValidTo() + ", maxRedemptions=" + this.getMaxRedemptions() + ", maxRedemptionsPerTenant=" + this.getMaxRedemptionsPerTenant() + ", firstSubscriptionOnly=" + this.getFirstSubscriptionOnly() + ", applicablePlanIds=" + this.getApplicablePlanIds() + ", metadataJson=" + this.getMetadataJson() + ", createdAt=" + this.getCreatedAt() + ", updatedAt=" + this.getUpdatedAt() + ")";
	}
}
