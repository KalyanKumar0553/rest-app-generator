package com.src.main.subscription.dto;

import java.time.LocalDateTime;
import com.src.main.subscription.enums.AuditActorType;
import com.src.main.subscription.enums.SubscriptionStatus;

public class SubscriptionAuditResponse {
	private Long id;
	private Long tenantId;
	private Long subscriptionId;
	private String eventType;
	private String oldPlanCode;
	private String newPlanCode;
	private SubscriptionStatus oldStatus;
	private SubscriptionStatus newStatus;
	private AuditActorType actorType;
	private String actorId;
	private String reason;
	private String payloadJson;
	private LocalDateTime createdAt;

	SubscriptionAuditResponse(final Long id, final Long tenantId, final Long subscriptionId, final String eventType, final String oldPlanCode, final String newPlanCode, final SubscriptionStatus oldStatus, final SubscriptionStatus newStatus, final AuditActorType actorType, final String actorId, final String reason, final String payloadJson, final LocalDateTime createdAt) {
		this.id = id;
		this.tenantId = tenantId;
		this.subscriptionId = subscriptionId;
		this.eventType = eventType;
		this.oldPlanCode = oldPlanCode;
		this.newPlanCode = newPlanCode;
		this.oldStatus = oldStatus;
		this.newStatus = newStatus;
		this.actorType = actorType;
		this.actorId = actorId;
		this.reason = reason;
		this.payloadJson = payloadJson;
		this.createdAt = createdAt;
	}


	public static class SubscriptionAuditResponseBuilder {
		private Long id;
		private Long tenantId;
		private Long subscriptionId;
		private String eventType;
		private String oldPlanCode;
		private String newPlanCode;
		private SubscriptionStatus oldStatus;
		private SubscriptionStatus newStatus;
		private AuditActorType actorType;
		private String actorId;
		private String reason;
		private String payloadJson;
		private LocalDateTime createdAt;

		SubscriptionAuditResponseBuilder() {
		}

		/**
		 * @return {@code this}.
		 */
		public SubscriptionAuditResponse.SubscriptionAuditResponseBuilder id(final Long id) {
			this.id = id;
			return this;
		}

		/**
		 * @return {@code this}.
		 */
		public SubscriptionAuditResponse.SubscriptionAuditResponseBuilder tenantId(final Long tenantId) {
			this.tenantId = tenantId;
			return this;
		}

		/**
		 * @return {@code this}.
		 */
		public SubscriptionAuditResponse.SubscriptionAuditResponseBuilder subscriptionId(final Long subscriptionId) {
			this.subscriptionId = subscriptionId;
			return this;
		}

		/**
		 * @return {@code this}.
		 */
		public SubscriptionAuditResponse.SubscriptionAuditResponseBuilder eventType(final String eventType) {
			this.eventType = eventType;
			return this;
		}

		/**
		 * @return {@code this}.
		 */
		public SubscriptionAuditResponse.SubscriptionAuditResponseBuilder oldPlanCode(final String oldPlanCode) {
			this.oldPlanCode = oldPlanCode;
			return this;
		}

		/**
		 * @return {@code this}.
		 */
		public SubscriptionAuditResponse.SubscriptionAuditResponseBuilder newPlanCode(final String newPlanCode) {
			this.newPlanCode = newPlanCode;
			return this;
		}

		/**
		 * @return {@code this}.
		 */
		public SubscriptionAuditResponse.SubscriptionAuditResponseBuilder oldStatus(final SubscriptionStatus oldStatus) {
			this.oldStatus = oldStatus;
			return this;
		}

		/**
		 * @return {@code this}.
		 */
		public SubscriptionAuditResponse.SubscriptionAuditResponseBuilder newStatus(final SubscriptionStatus newStatus) {
			this.newStatus = newStatus;
			return this;
		}

		/**
		 * @return {@code this}.
		 */
		public SubscriptionAuditResponse.SubscriptionAuditResponseBuilder actorType(final AuditActorType actorType) {
			this.actorType = actorType;
			return this;
		}

		/**
		 * @return {@code this}.
		 */
		public SubscriptionAuditResponse.SubscriptionAuditResponseBuilder actorId(final String actorId) {
			this.actorId = actorId;
			return this;
		}

		/**
		 * @return {@code this}.
		 */
		public SubscriptionAuditResponse.SubscriptionAuditResponseBuilder reason(final String reason) {
			this.reason = reason;
			return this;
		}

		/**
		 * @return {@code this}.
		 */
		public SubscriptionAuditResponse.SubscriptionAuditResponseBuilder payloadJson(final String payloadJson) {
			this.payloadJson = payloadJson;
			return this;
		}

		/**
		 * @return {@code this}.
		 */
		public SubscriptionAuditResponse.SubscriptionAuditResponseBuilder createdAt(final LocalDateTime createdAt) {
			this.createdAt = createdAt;
			return this;
		}

		public SubscriptionAuditResponse build() {
			return new SubscriptionAuditResponse(this.id, this.tenantId, this.subscriptionId, this.eventType, this.oldPlanCode, this.newPlanCode, this.oldStatus, this.newStatus, this.actorType, this.actorId, this.reason, this.payloadJson, this.createdAt);
		}

		@Override
		public String toString() {
			return "SubscriptionAuditResponse.SubscriptionAuditResponseBuilder(id=" + this.id + ", tenantId=" + this.tenantId + ", subscriptionId=" + this.subscriptionId + ", eventType=" + this.eventType + ", oldPlanCode=" + this.oldPlanCode + ", newPlanCode=" + this.newPlanCode + ", oldStatus=" + this.oldStatus + ", newStatus=" + this.newStatus + ", actorType=" + this.actorType + ", actorId=" + this.actorId + ", reason=" + this.reason + ", payloadJson=" + this.payloadJson + ", createdAt=" + this.createdAt + ")";
		}
	}

	public static SubscriptionAuditResponse.SubscriptionAuditResponseBuilder builder() {
		return new SubscriptionAuditResponse.SubscriptionAuditResponseBuilder();
	}

	public Long getId() {
		return this.id;
	}

	public Long getTenantId() {
		return this.tenantId;
	}

	public Long getSubscriptionId() {
		return this.subscriptionId;
	}

	public String getEventType() {
		return this.eventType;
	}

	public String getOldPlanCode() {
		return this.oldPlanCode;
	}

	public String getNewPlanCode() {
		return this.newPlanCode;
	}

	public SubscriptionStatus getOldStatus() {
		return this.oldStatus;
	}

	public SubscriptionStatus getNewStatus() {
		return this.newStatus;
	}

	public AuditActorType getActorType() {
		return this.actorType;
	}

	public String getActorId() {
		return this.actorId;
	}

	public String getReason() {
		return this.reason;
	}

	public String getPayloadJson() {
		return this.payloadJson;
	}

	public LocalDateTime getCreatedAt() {
		return this.createdAt;
	}

	public void setId(final Long id) {
		this.id = id;
	}

	public void setTenantId(final Long tenantId) {
		this.tenantId = tenantId;
	}

	public void setSubscriptionId(final Long subscriptionId) {
		this.subscriptionId = subscriptionId;
	}

	public void setEventType(final String eventType) {
		this.eventType = eventType;
	}

	public void setOldPlanCode(final String oldPlanCode) {
		this.oldPlanCode = oldPlanCode;
	}

	public void setNewPlanCode(final String newPlanCode) {
		this.newPlanCode = newPlanCode;
	}

	public void setOldStatus(final SubscriptionStatus oldStatus) {
		this.oldStatus = oldStatus;
	}

	public void setNewStatus(final SubscriptionStatus newStatus) {
		this.newStatus = newStatus;
	}

	public void setActorType(final AuditActorType actorType) {
		this.actorType = actorType;
	}

	public void setActorId(final String actorId) {
		this.actorId = actorId;
	}

	public void setReason(final String reason) {
		this.reason = reason;
	}

	public void setPayloadJson(final String payloadJson) {
		this.payloadJson = payloadJson;
	}

	public void setCreatedAt(final LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	@Override
	public boolean equals(final Object o) {
		if (o == this) return true;
		if (!(o instanceof SubscriptionAuditResponse)) return false;
		final SubscriptionAuditResponse other = (SubscriptionAuditResponse) o;
		if (!other.canEqual((Object) this)) return false;
		final Object this$id = this.getId();
		final Object other$id = other.getId();
		if (this$id == null ? other$id != null : !this$id.equals(other$id)) return false;
		final Object this$tenantId = this.getTenantId();
		final Object other$tenantId = other.getTenantId();
		if (this$tenantId == null ? other$tenantId != null : !this$tenantId.equals(other$tenantId)) return false;
		final Object this$subscriptionId = this.getSubscriptionId();
		final Object other$subscriptionId = other.getSubscriptionId();
		if (this$subscriptionId == null ? other$subscriptionId != null : !this$subscriptionId.equals(other$subscriptionId)) return false;
		final Object this$eventType = this.getEventType();
		final Object other$eventType = other.getEventType();
		if (this$eventType == null ? other$eventType != null : !this$eventType.equals(other$eventType)) return false;
		final Object this$oldPlanCode = this.getOldPlanCode();
		final Object other$oldPlanCode = other.getOldPlanCode();
		if (this$oldPlanCode == null ? other$oldPlanCode != null : !this$oldPlanCode.equals(other$oldPlanCode)) return false;
		final Object this$newPlanCode = this.getNewPlanCode();
		final Object other$newPlanCode = other.getNewPlanCode();
		if (this$newPlanCode == null ? other$newPlanCode != null : !this$newPlanCode.equals(other$newPlanCode)) return false;
		final Object this$oldStatus = this.getOldStatus();
		final Object other$oldStatus = other.getOldStatus();
		if (this$oldStatus == null ? other$oldStatus != null : !this$oldStatus.equals(other$oldStatus)) return false;
		final Object this$newStatus = this.getNewStatus();
		final Object other$newStatus = other.getNewStatus();
		if (this$newStatus == null ? other$newStatus != null : !this$newStatus.equals(other$newStatus)) return false;
		final Object this$actorType = this.getActorType();
		final Object other$actorType = other.getActorType();
		if (this$actorType == null ? other$actorType != null : !this$actorType.equals(other$actorType)) return false;
		final Object this$actorId = this.getActorId();
		final Object other$actorId = other.getActorId();
		if (this$actorId == null ? other$actorId != null : !this$actorId.equals(other$actorId)) return false;
		final Object this$reason = this.getReason();
		final Object other$reason = other.getReason();
		if (this$reason == null ? other$reason != null : !this$reason.equals(other$reason)) return false;
		final Object this$payloadJson = this.getPayloadJson();
		final Object other$payloadJson = other.getPayloadJson();
		if (this$payloadJson == null ? other$payloadJson != null : !this$payloadJson.equals(other$payloadJson)) return false;
		final Object this$createdAt = this.getCreatedAt();
		final Object other$createdAt = other.getCreatedAt();
		if (this$createdAt == null ? other$createdAt != null : !this$createdAt.equals(other$createdAt)) return false;
		return true;
	}

	protected boolean canEqual(final Object other) {
		return other instanceof SubscriptionAuditResponse;
	}

	@Override
	public int hashCode() {
		final int PRIME = 59;
		int result = 1;
		final Object $id = this.getId();
		result = result * PRIME + ($id == null ? 43 : $id.hashCode());
		final Object $tenantId = this.getTenantId();
		result = result * PRIME + ($tenantId == null ? 43 : $tenantId.hashCode());
		final Object $subscriptionId = this.getSubscriptionId();
		result = result * PRIME + ($subscriptionId == null ? 43 : $subscriptionId.hashCode());
		final Object $eventType = this.getEventType();
		result = result * PRIME + ($eventType == null ? 43 : $eventType.hashCode());
		final Object $oldPlanCode = this.getOldPlanCode();
		result = result * PRIME + ($oldPlanCode == null ? 43 : $oldPlanCode.hashCode());
		final Object $newPlanCode = this.getNewPlanCode();
		result = result * PRIME + ($newPlanCode == null ? 43 : $newPlanCode.hashCode());
		final Object $oldStatus = this.getOldStatus();
		result = result * PRIME + ($oldStatus == null ? 43 : $oldStatus.hashCode());
		final Object $newStatus = this.getNewStatus();
		result = result * PRIME + ($newStatus == null ? 43 : $newStatus.hashCode());
		final Object $actorType = this.getActorType();
		result = result * PRIME + ($actorType == null ? 43 : $actorType.hashCode());
		final Object $actorId = this.getActorId();
		result = result * PRIME + ($actorId == null ? 43 : $actorId.hashCode());
		final Object $reason = this.getReason();
		result = result * PRIME + ($reason == null ? 43 : $reason.hashCode());
		final Object $payloadJson = this.getPayloadJson();
		result = result * PRIME + ($payloadJson == null ? 43 : $payloadJson.hashCode());
		final Object $createdAt = this.getCreatedAt();
		result = result * PRIME + ($createdAt == null ? 43 : $createdAt.hashCode());
		return result;
	}

	@Override
	public String toString() {
		return "SubscriptionAuditResponse(id=" + this.getId() + ", tenantId=" + this.getTenantId() + ", subscriptionId=" + this.getSubscriptionId() + ", eventType=" + this.getEventType() + ", oldPlanCode=" + this.getOldPlanCode() + ", newPlanCode=" + this.getNewPlanCode() + ", oldStatus=" + this.getOldStatus() + ", newStatus=" + this.getNewStatus() + ", actorType=" + this.getActorType() + ", actorId=" + this.getActorId() + ", reason=" + this.getReason() + ", payloadJson=" + this.getPayloadJson() + ", createdAt=" + this.getCreatedAt() + ")";
	}
}
