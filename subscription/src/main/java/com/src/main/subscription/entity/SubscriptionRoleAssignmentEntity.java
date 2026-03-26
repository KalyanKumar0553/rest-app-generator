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
import jakarta.persistence.Table;

@Entity
@Table(name = "subscription_role_assignment")
public class SubscriptionRoleAssignmentEntity extends BaseSubscriptionEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Column(name = "tenant_id", nullable = false)
	private Long tenantId;
	@Column(name = "user_id", nullable = false)
	private String userId;
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "subscription_id", nullable = false)
	private CustomerSubscriptionEntity subscription;
	@Column(name = "role_name", nullable = false)
	private String roleName;
	@Column(name = "is_active", nullable = false)
	private Boolean isActive = Boolean.TRUE;
	@Column(name = "assigned_at", nullable = false)
	private LocalDateTime assignedAt;
	@Column(name = "revoked_at")
	private LocalDateTime revokedAt;
	@Column(name = "metadata_json", columnDefinition = "text")
	private String metadataJson;

	public Long getId() {
		return this.id;
	}

	public Long getTenantId() {
		return this.tenantId;
	}

	public String getUserId() {
		return this.userId;
	}

	public CustomerSubscriptionEntity getSubscription() {
		return this.subscription;
	}

	public String getRoleName() {
		return this.roleName;
	}

	public Boolean getIsActive() {
		return this.isActive;
	}

	public LocalDateTime getAssignedAt() {
		return this.assignedAt;
	}

	public LocalDateTime getRevokedAt() {
		return this.revokedAt;
	}

	public String getMetadataJson() {
		return this.metadataJson;
	}

	public void setId(final Long id) {
		this.id = id;
	}

	public void setTenantId(final Long tenantId) {
		this.tenantId = tenantId;
	}

	public void setUserId(final String userId) {
		this.userId = userId;
	}

	public void setSubscription(final CustomerSubscriptionEntity subscription) {
		this.subscription = subscription;
	}

	public void setRoleName(final String roleName) {
		this.roleName = roleName;
	}

	public void setIsActive(final Boolean isActive) {
		this.isActive = isActive;
	}

	public void setAssignedAt(final LocalDateTime assignedAt) {
		this.assignedAt = assignedAt;
	}

	public void setRevokedAt(final LocalDateTime revokedAt) {
		this.revokedAt = revokedAt;
	}

	public void setMetadataJson(final String metadataJson) {
		this.metadataJson = metadataJson;
	}
}
