package com.src.main.subscription.entity;

import java.time.LocalDateTime;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Version;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseSubscriptionEntity {
	@CreatedDate
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;
	@CreatedBy
	@Column(name = "created_by", updatable = false, length = 100)
	private String createdBy;
	@LastModifiedDate
	@Column(name = "updated_at")
	private LocalDateTime updatedAt;
	@LastModifiedBy
	@Column(name = "updated_by", length = 100)
	private String updatedBy;
	@Column(name = "deleted", nullable = false)
	private Boolean deleted = Boolean.FALSE;
	@Version
	@Column(name = "entity_version")
	private Long entityVersion;

	@PrePersist
	void onCreate() {
		LocalDateTime now = LocalDateTime.now();
		if (createdAt == null) {
			createdAt = now;
		}
		if (updatedAt == null) {
			updatedAt = now;
		}
		if (deleted == null) {
			deleted = Boolean.FALSE;
		}
	}

	public LocalDateTime getCreatedAt() {
		return this.createdAt;
	}

	public String getCreatedBy() {
		return this.createdBy;
	}

	public LocalDateTime getUpdatedAt() {
		return this.updatedAt;
	}

	public String getUpdatedBy() {
		return this.updatedBy;
	}

	public Boolean getDeleted() {
		return this.deleted;
	}

	public Long getEntityVersion() {
		return this.entityVersion;
	}

	public void setCreatedAt(final LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public void setCreatedBy(final String createdBy) {
		this.createdBy = createdBy;
	}

	public void setUpdatedAt(final LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}

	public void setUpdatedBy(final String updatedBy) {
		this.updatedBy = updatedBy;
	}

	public void setDeleted(final Boolean deleted) {
		this.deleted = deleted;
	}

	public void setEntityVersion(final Long entityVersion) {
		this.entityVersion = entityVersion;
	}
}
