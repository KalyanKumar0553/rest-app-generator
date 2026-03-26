package com.src.main.subscription.entity;

import java.time.LocalDateTime;
import com.src.main.subscription.enums.AuditActorType;
import com.src.main.subscription.enums.SubscriptionStatus;
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
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "subscription_audit_log")
public class SubscriptionAuditLogEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Column(name = "tenant_id", nullable = false)
	private Long tenantId;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "subscription_id")
	private CustomerSubscriptionEntity subscription;
	@Column(name = "event_type", nullable = false, length = 100)
	private String eventType;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "old_plan_id")
	private SubscriptionPlanEntity oldPlan;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "new_plan_id")
	private SubscriptionPlanEntity newPlan;
	@Enumerated(EnumType.STRING)
	@Column(name = "old_status", length = 50)
	private SubscriptionStatus oldStatus;
	@Enumerated(EnumType.STRING)
	@Column(name = "new_status", length = 50)
	private SubscriptionStatus newStatus;
	@Enumerated(EnumType.STRING)
	@Column(name = "actor_type", nullable = false, length = 50)
	private AuditActorType actorType;
	@Column(name = "actor_id", length = 100)
	private String actorId;
	@Column(name = "reason", length = 500)
	private String reason;
	@Column(name = "payload_json", columnDefinition = "text")
	private String payloadJson;
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@PrePersist
	void onCreate() {
		if (createdAt == null) {
			createdAt = LocalDateTime.now();
		}
	}

	public Long getId() {
		return this.id;
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

	public LocalDateTime getCreatedAt() {
		return this.createdAt;
	}

	public void setId(final Long id) {
		this.id = id;
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

	public void setCreatedAt(final LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}
}
