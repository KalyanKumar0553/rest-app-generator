package com.src.main.model;

import java.time.OffsetDateTime;
import com.src.main.config.AppDbTables;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = AppDbTables.AI_LABS_USAGE)
public class AiLabsUsageEntity {
	@Id
	@Column(name = "owner_user_id", nullable = false, length = 100)
	private String ownerUserId;
	@Column(name = "usage_count", nullable = false)
	private int usageCount;
	@Column(name = "created_at", nullable = false)
	private OffsetDateTime createdAt;
	@Column(name = "updated_at", nullable = false)
	private OffsetDateTime updatedAt;

	@PrePersist
	public void prePersist() {
		OffsetDateTime now = OffsetDateTime.now();
		if (createdAt == null) {
			createdAt = now;
		}
		if (updatedAt == null) {
			updatedAt = now;
		}
	}

	@PreUpdate
	public void preUpdate() {
		updatedAt = OffsetDateTime.now();
	}

	public AiLabsUsageEntity() {
	}

	public String getOwnerUserId() {
		return this.ownerUserId;
	}

	public int getUsageCount() {
		return this.usageCount;
	}

	public OffsetDateTime getCreatedAt() {
		return this.createdAt;
	}

	public OffsetDateTime getUpdatedAt() {
		return this.updatedAt;
	}

	public void setOwnerUserId(final String ownerUserId) {
		this.ownerUserId = ownerUserId;
	}

	public void setUsageCount(final int usageCount) {
		this.usageCount = usageCount;
	}

	public void setCreatedAt(final OffsetDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public void setUpdatedAt(final OffsetDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}

	@Override
	public boolean equals(final Object o) {
		if (o == this) return true;
		if (!(o instanceof AiLabsUsageEntity)) return false;
		final AiLabsUsageEntity other = (AiLabsUsageEntity) o;
		if (!other.canEqual((Object) this)) return false;
		if (this.getUsageCount() != other.getUsageCount()) return false;
		final Object this$ownerUserId = this.getOwnerUserId();
		final Object other$ownerUserId = other.getOwnerUserId();
		if (this$ownerUserId == null ? other$ownerUserId != null : !this$ownerUserId.equals(other$ownerUserId)) return false;
		final Object this$createdAt = this.getCreatedAt();
		final Object other$createdAt = other.getCreatedAt();
		if (this$createdAt == null ? other$createdAt != null : !this$createdAt.equals(other$createdAt)) return false;
		final Object this$updatedAt = this.getUpdatedAt();
		final Object other$updatedAt = other.getUpdatedAt();
		if (this$updatedAt == null ? other$updatedAt != null : !this$updatedAt.equals(other$updatedAt)) return false;
		return true;
	}

	protected boolean canEqual(final Object other) {
		return other instanceof AiLabsUsageEntity;
	}

	@Override
	public int hashCode() {
		final int PRIME = 59;
		int result = 1;
		result = result * PRIME + this.getUsageCount();
		final Object $ownerUserId = this.getOwnerUserId();
		result = result * PRIME + ($ownerUserId == null ? 43 : $ownerUserId.hashCode());
		final Object $createdAt = this.getCreatedAt();
		result = result * PRIME + ($createdAt == null ? 43 : $createdAt.hashCode());
		final Object $updatedAt = this.getUpdatedAt();
		result = result * PRIME + ($updatedAt == null ? 43 : $updatedAt.hashCode());
		return result;
	}

	@Override
	public String toString() {
		return "AiLabsUsageEntity(ownerUserId=" + this.getOwnerUserId() + ", usageCount=" + this.getUsageCount() + ", createdAt=" + this.getCreatedAt() + ", updatedAt=" + this.getUpdatedAt() + ")";
	}
}
