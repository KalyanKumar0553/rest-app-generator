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
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "subscription_audit_log")
@Getter
@Setter
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
}
