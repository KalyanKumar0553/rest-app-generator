package com.src.main.subscription.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "subscription_plan_role_mapping")
public class SubscriptionPlanRoleMappingEntity extends BaseSubscriptionEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "plan_id", nullable = false)
	private SubscriptionPlanEntity plan;
	@Column(name = "role_name", nullable = false)
	private String roleName;

	public Long getId() {
		return this.id;
	}

	public SubscriptionPlanEntity getPlan() {
		return this.plan;
	}

	public String getRoleName() {
		return this.roleName;
	}

	public void setId(final Long id) {
		this.id = id;
	}

	public void setPlan(final SubscriptionPlanEntity plan) {
		this.plan = plan;
	}

	public void setRoleName(final String roleName) {
		this.roleName = roleName;
	}
}
