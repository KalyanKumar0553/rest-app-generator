package com.src.main.subscription.dto;

import com.src.main.subscription.entity.CustomerSubscriptionEntity;
import com.src.main.subscription.entity.SubscriptionPlanEntity;
import com.src.main.subscription.enums.AuditActorType;
import com.src.main.subscription.enums.SubscriptionStatus;

public class SubscriptionAuditCommand {
	private Long tenantId;
	private CustomerSubscriptionEntity subscription;
	private String eventType;
	private SubscriptionPlanEntity oldPlan;
	private SubscriptionPlanEntity newPlan;
	private SubscriptionStatus oldStatus;
	private SubscriptionStatus newStatus;
	private AuditActorType actorType;
	private String actorId;
	private String reason;
	private String payloadJson;

	SubscriptionAuditCommand(final Long tenantId, final CustomerSubscriptionEntity subscription, final String eventType, final SubscriptionPlanEntity oldPlan, final SubscriptionPlanEntity newPlan, final SubscriptionStatus oldStatus, final SubscriptionStatus newStatus, final AuditActorType actorType, final String actorId, final String reason, final String payloadJson) {
		this.tenantId = tenantId;
		this.subscription = subscription;
		this.eventType = eventType;
		this.oldPlan = oldPlan;
		this.newPlan = newPlan;
		this.oldStatus = oldStatus;
		this.newStatus = newStatus;
		this.actorType = actorType;
		this.actorId = actorId;
		this.reason = reason;
		this.payloadJson = payloadJson;
	}


	public static class SubscriptionAuditCommandBuilder {
		private Long tenantId;
		private CustomerSubscriptionEntity subscription;
		private String eventType;
		private SubscriptionPlanEntity oldPlan;
		private SubscriptionPlanEntity newPlan;
		private SubscriptionStatus oldStatus;
		private SubscriptionStatus newStatus;
		private AuditActorType actorType;
		private String actorId;
		private String reason;
		private String payloadJson;

		SubscriptionAuditCommandBuilder() {
		}

		/**
		 * @return {@code this}.
		 */
		public SubscriptionAuditCommand.SubscriptionAuditCommandBuilder tenantId(final Long tenantId) {
			this.tenantId = tenantId;
			return this;
		}

		/**
		 * @return {@code this}.
		 */
		public SubscriptionAuditCommand.SubscriptionAuditCommandBuilder subscription(final CustomerSubscriptionEntity subscription) {
			this.subscription = subscription;
			return this;
		}

		/**
		 * @return {@code this}.
		 */
		public SubscriptionAuditCommand.SubscriptionAuditCommandBuilder eventType(final String eventType) {
			this.eventType = eventType;
			return this;
		}

		/**
		 * @return {@code this}.
		 */
		public SubscriptionAuditCommand.SubscriptionAuditCommandBuilder oldPlan(final SubscriptionPlanEntity oldPlan) {
			this.oldPlan = oldPlan;
			return this;
		}

		/**
		 * @return {@code this}.
		 */
		public SubscriptionAuditCommand.SubscriptionAuditCommandBuilder newPlan(final SubscriptionPlanEntity newPlan) {
			this.newPlan = newPlan;
			return this;
		}

		/**
		 * @return {@code this}.
		 */
		public SubscriptionAuditCommand.SubscriptionAuditCommandBuilder oldStatus(final SubscriptionStatus oldStatus) {
			this.oldStatus = oldStatus;
			return this;
		}

		/**
		 * @return {@code this}.
		 */
		public SubscriptionAuditCommand.SubscriptionAuditCommandBuilder newStatus(final SubscriptionStatus newStatus) {
			this.newStatus = newStatus;
			return this;
		}

		/**
		 * @return {@code this}.
		 */
		public SubscriptionAuditCommand.SubscriptionAuditCommandBuilder actorType(final AuditActorType actorType) {
			this.actorType = actorType;
			return this;
		}

		/**
		 * @return {@code this}.
		 */
		public SubscriptionAuditCommand.SubscriptionAuditCommandBuilder actorId(final String actorId) {
			this.actorId = actorId;
			return this;
		}

		/**
		 * @return {@code this}.
		 */
		public SubscriptionAuditCommand.SubscriptionAuditCommandBuilder reason(final String reason) {
			this.reason = reason;
			return this;
		}

		/**
		 * @return {@code this}.
		 */
		public SubscriptionAuditCommand.SubscriptionAuditCommandBuilder payloadJson(final String payloadJson) {
			this.payloadJson = payloadJson;
			return this;
		}

		public SubscriptionAuditCommand build() {
			return new SubscriptionAuditCommand(this.tenantId, this.subscription, this.eventType, this.oldPlan, this.newPlan, this.oldStatus, this.newStatus, this.actorType, this.actorId, this.reason, this.payloadJson);
		}

		@Override
		public String toString() {
			return "SubscriptionAuditCommand.SubscriptionAuditCommandBuilder(tenantId=" + this.tenantId + ", subscription=" + this.subscription + ", eventType=" + this.eventType + ", oldPlan=" + this.oldPlan + ", newPlan=" + this.newPlan + ", oldStatus=" + this.oldStatus + ", newStatus=" + this.newStatus + ", actorType=" + this.actorType + ", actorId=" + this.actorId + ", reason=" + this.reason + ", payloadJson=" + this.payloadJson + ")";
		}
	}

	public static SubscriptionAuditCommand.SubscriptionAuditCommandBuilder builder() {
		return new SubscriptionAuditCommand.SubscriptionAuditCommandBuilder();
	}

	public Long getTenantId() {
		return this.tenantId;
	}

	public CustomerSubscriptionEntity getSubscription() {
		return this.subscription;
	}

	public String getEventType() {
		return this.eventType;
	}

	public SubscriptionPlanEntity getOldPlan() {
		return this.oldPlan;
	}

	public SubscriptionPlanEntity getNewPlan() {
		return this.newPlan;
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

	public void setTenantId(final Long tenantId) {
		this.tenantId = tenantId;
	}

	public void setSubscription(final CustomerSubscriptionEntity subscription) {
		this.subscription = subscription;
	}

	public void setEventType(final String eventType) {
		this.eventType = eventType;
	}

	public void setOldPlan(final SubscriptionPlanEntity oldPlan) {
		this.oldPlan = oldPlan;
	}

	public void setNewPlan(final SubscriptionPlanEntity newPlan) {
		this.newPlan = newPlan;
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

	@Override
	public boolean equals(final Object o) {
		if (o == this) return true;
		if (!(o instanceof SubscriptionAuditCommand)) return false;
		final SubscriptionAuditCommand other = (SubscriptionAuditCommand) o;
		if (!other.canEqual((Object) this)) return false;
		final Object this$tenantId = this.getTenantId();
		final Object other$tenantId = other.getTenantId();
		if (this$tenantId == null ? other$tenantId != null : !this$tenantId.equals(other$tenantId)) return false;
		final Object this$subscription = this.getSubscription();
		final Object other$subscription = other.getSubscription();
		if (this$subscription == null ? other$subscription != null : !this$subscription.equals(other$subscription)) return false;
		final Object this$eventType = this.getEventType();
		final Object other$eventType = other.getEventType();
		if (this$eventType == null ? other$eventType != null : !this$eventType.equals(other$eventType)) return false;
		final Object this$oldPlan = this.getOldPlan();
		final Object other$oldPlan = other.getOldPlan();
		if (this$oldPlan == null ? other$oldPlan != null : !this$oldPlan.equals(other$oldPlan)) return false;
		final Object this$newPlan = this.getNewPlan();
		final Object other$newPlan = other.getNewPlan();
		if (this$newPlan == null ? other$newPlan != null : !this$newPlan.equals(other$newPlan)) return false;
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
		return true;
	}

	protected boolean canEqual(final Object other) {
		return other instanceof SubscriptionAuditCommand;
	}

	@Override
	public int hashCode() {
		final int PRIME = 59;
		int result = 1;
		final Object $tenantId = this.getTenantId();
		result = result * PRIME + ($tenantId == null ? 43 : $tenantId.hashCode());
		final Object $subscription = this.getSubscription();
		result = result * PRIME + ($subscription == null ? 43 : $subscription.hashCode());
		final Object $eventType = this.getEventType();
		result = result * PRIME + ($eventType == null ? 43 : $eventType.hashCode());
		final Object $oldPlan = this.getOldPlan();
		result = result * PRIME + ($oldPlan == null ? 43 : $oldPlan.hashCode());
		final Object $newPlan = this.getNewPlan();
		result = result * PRIME + ($newPlan == null ? 43 : $newPlan.hashCode());
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
		return result;
	}

	@Override
	public String toString() {
		return "SubscriptionAuditCommand(tenantId=" + this.getTenantId() + ", subscription=" + this.getSubscription() + ", eventType=" + this.getEventType() + ", oldPlan=" + this.getOldPlan() + ", newPlan=" + this.getNewPlan() + ", oldStatus=" + this.getOldStatus() + ", newStatus=" + this.getNewStatus() + ", actorType=" + this.getActorType() + ", actorId=" + this.getActorId() + ", reason=" + this.getReason() + ", payloadJson=" + this.getPayloadJson() + ")";
	}
}
