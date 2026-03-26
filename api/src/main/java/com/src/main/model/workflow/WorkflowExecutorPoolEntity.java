package com.src.main.model.workflow;

import java.time.OffsetDateTime;
import java.util.UUID;
import org.hibernate.annotations.UuidGenerator;
import com.src.main.config.AppDbTables;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = AppDbTables.WORKFLOW_EXECUTOR_POOLS, indexes = {@Index(name = "idx_workflow_pool_code_active", columnList = "pool_code, active")})
public class WorkflowExecutorPoolEntity {
	@Id
	@UuidGenerator
	private UUID id;
	@Column(name = "pool_code", nullable = false, unique = true, length = 120)
	private String poolCode;
	@Column(name = "pool_name", nullable = false, length = 200)
	private String poolName;
	@Column(name = "core_pool_size", nullable = false)
	private int corePoolSize;
	@Column(name = "max_pool_size", nullable = false)
	private int maxPoolSize;
	@Column(name = "queue_capacity", nullable = false)
	private int queueCapacity;
	@Column(name = "keep_alive_seconds", nullable = false)
	private int keepAliveSeconds;
	@Column(name = "active", nullable = false)
	private boolean active;
	@Column(name = "created_at", nullable = false, updatable = false)
	private OffsetDateTime createdAt;
	@Column(name = "updated_at", nullable = false)
	private OffsetDateTime updatedAt;

	@PrePersist
	void onCreate() {
		createdAt = OffsetDateTime.now();
		updatedAt = createdAt;
	}

	@PreUpdate
	void onUpdate() {
		updatedAt = OffsetDateTime.now();
	}

	public WorkflowExecutorPoolEntity() {
	}

	public UUID getId() {
		return this.id;
	}

	public String getPoolCode() {
		return this.poolCode;
	}

	public String getPoolName() {
		return this.poolName;
	}

	public int getCorePoolSize() {
		return this.corePoolSize;
	}

	public int getMaxPoolSize() {
		return this.maxPoolSize;
	}

	public int getQueueCapacity() {
		return this.queueCapacity;
	}

	public int getKeepAliveSeconds() {
		return this.keepAliveSeconds;
	}

	public boolean isActive() {
		return this.active;
	}

	public OffsetDateTime getCreatedAt() {
		return this.createdAt;
	}

	public OffsetDateTime getUpdatedAt() {
		return this.updatedAt;
	}

	public void setId(final UUID id) {
		this.id = id;
	}

	public void setPoolCode(final String poolCode) {
		this.poolCode = poolCode;
	}

	public void setPoolName(final String poolName) {
		this.poolName = poolName;
	}

	public void setCorePoolSize(final int corePoolSize) {
		this.corePoolSize = corePoolSize;
	}

	public void setMaxPoolSize(final int maxPoolSize) {
		this.maxPoolSize = maxPoolSize;
	}

	public void setQueueCapacity(final int queueCapacity) {
		this.queueCapacity = queueCapacity;
	}

	public void setKeepAliveSeconds(final int keepAliveSeconds) {
		this.keepAliveSeconds = keepAliveSeconds;
	}

	public void setActive(final boolean active) {
		this.active = active;
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
		if (!(o instanceof WorkflowExecutorPoolEntity)) return false;
		final WorkflowExecutorPoolEntity other = (WorkflowExecutorPoolEntity) o;
		if (!other.canEqual((Object) this)) return false;
		if (this.getCorePoolSize() != other.getCorePoolSize()) return false;
		if (this.getMaxPoolSize() != other.getMaxPoolSize()) return false;
		if (this.getQueueCapacity() != other.getQueueCapacity()) return false;
		if (this.getKeepAliveSeconds() != other.getKeepAliveSeconds()) return false;
		if (this.isActive() != other.isActive()) return false;
		final Object this$id = this.getId();
		final Object other$id = other.getId();
		if (this$id == null ? other$id != null : !this$id.equals(other$id)) return false;
		final Object this$poolCode = this.getPoolCode();
		final Object other$poolCode = other.getPoolCode();
		if (this$poolCode == null ? other$poolCode != null : !this$poolCode.equals(other$poolCode)) return false;
		final Object this$poolName = this.getPoolName();
		final Object other$poolName = other.getPoolName();
		if (this$poolName == null ? other$poolName != null : !this$poolName.equals(other$poolName)) return false;
		final Object this$createdAt = this.getCreatedAt();
		final Object other$createdAt = other.getCreatedAt();
		if (this$createdAt == null ? other$createdAt != null : !this$createdAt.equals(other$createdAt)) return false;
		final Object this$updatedAt = this.getUpdatedAt();
		final Object other$updatedAt = other.getUpdatedAt();
		if (this$updatedAt == null ? other$updatedAt != null : !this$updatedAt.equals(other$updatedAt)) return false;
		return true;
	}

	protected boolean canEqual(final Object other) {
		return other instanceof WorkflowExecutorPoolEntity;
	}

	@Override
	public int hashCode() {
		final int PRIME = 59;
		int result = 1;
		result = result * PRIME + this.getCorePoolSize();
		result = result * PRIME + this.getMaxPoolSize();
		result = result * PRIME + this.getQueueCapacity();
		result = result * PRIME + this.getKeepAliveSeconds();
		result = result * PRIME + (this.isActive() ? 79 : 97);
		final Object $id = this.getId();
		result = result * PRIME + ($id == null ? 43 : $id.hashCode());
		final Object $poolCode = this.getPoolCode();
		result = result * PRIME + ($poolCode == null ? 43 : $poolCode.hashCode());
		final Object $poolName = this.getPoolName();
		result = result * PRIME + ($poolName == null ? 43 : $poolName.hashCode());
		final Object $createdAt = this.getCreatedAt();
		result = result * PRIME + ($createdAt == null ? 43 : $createdAt.hashCode());
		final Object $updatedAt = this.getUpdatedAt();
		result = result * PRIME + ($updatedAt == null ? 43 : $updatedAt.hashCode());
		return result;
	}

	@Override
	public String toString() {
		return "WorkflowExecutorPoolEntity(id=" + this.getId() + ", poolCode=" + this.getPoolCode() + ", poolName=" + this.getPoolName() + ", corePoolSize=" + this.getCorePoolSize() + ", maxPoolSize=" + this.getMaxPoolSize() + ", queueCapacity=" + this.getQueueCapacity() + ", keepAliveSeconds=" + this.getKeepAliveSeconds() + ", active=" + this.isActive() + ", createdAt=" + this.getCreatedAt() + ", updatedAt=" + this.getUpdatedAt() + ")";
	}
}
