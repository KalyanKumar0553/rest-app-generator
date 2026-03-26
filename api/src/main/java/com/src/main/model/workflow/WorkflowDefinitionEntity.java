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
@Table(name = AppDbTables.WORKFLOW_DEFINITIONS, indexes = {@Index(name = "idx_workflow_def_language_active", columnList = "language, active"), @Index(name = "idx_workflow_def_code_version", columnList = "code, version")})
public class WorkflowDefinitionEntity {
	@Id
	@UuidGenerator
	private UUID id;
	@Column(name = "code", nullable = false, length = 120)
	private String code;
	@Column(name = "name", nullable = false, length = 200)
	private String name;
	@Column(name = "language", nullable = false, length = 50)
	private String language;
	@Column(name = "version", nullable = false)
	private int version;
	@Column(name = "active", nullable = false)
	private boolean active;
	@Column(name = "dispatch_pool_code", nullable = false, length = 120)
	private String dispatchPoolCode;
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

	public WorkflowDefinitionEntity() {
	}

	public UUID getId() {
		return this.id;
	}

	public String getCode() {
		return this.code;
	}

	public String getName() {
		return this.name;
	}

	public String getLanguage() {
		return this.language;
	}

	public int getVersion() {
		return this.version;
	}

	public boolean isActive() {
		return this.active;
	}

	public String getDispatchPoolCode() {
		return this.dispatchPoolCode;
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

	public void setCode(final String code) {
		this.code = code;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public void setLanguage(final String language) {
		this.language = language;
	}

	public void setVersion(final int version) {
		this.version = version;
	}

	public void setActive(final boolean active) {
		this.active = active;
	}

	public void setDispatchPoolCode(final String dispatchPoolCode) {
		this.dispatchPoolCode = dispatchPoolCode;
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
		if (!(o instanceof WorkflowDefinitionEntity)) return false;
		final WorkflowDefinitionEntity other = (WorkflowDefinitionEntity) o;
		if (!other.canEqual((Object) this)) return false;
		if (this.getVersion() != other.getVersion()) return false;
		if (this.isActive() != other.isActive()) return false;
		final Object this$id = this.getId();
		final Object other$id = other.getId();
		if (this$id == null ? other$id != null : !this$id.equals(other$id)) return false;
		final Object this$code = this.getCode();
		final Object other$code = other.getCode();
		if (this$code == null ? other$code != null : !this$code.equals(other$code)) return false;
		final Object this$name = this.getName();
		final Object other$name = other.getName();
		if (this$name == null ? other$name != null : !this$name.equals(other$name)) return false;
		final Object this$language = this.getLanguage();
		final Object other$language = other.getLanguage();
		if (this$language == null ? other$language != null : !this$language.equals(other$language)) return false;
		final Object this$dispatchPoolCode = this.getDispatchPoolCode();
		final Object other$dispatchPoolCode = other.getDispatchPoolCode();
		if (this$dispatchPoolCode == null ? other$dispatchPoolCode != null : !this$dispatchPoolCode.equals(other$dispatchPoolCode)) return false;
		final Object this$createdAt = this.getCreatedAt();
		final Object other$createdAt = other.getCreatedAt();
		if (this$createdAt == null ? other$createdAt != null : !this$createdAt.equals(other$createdAt)) return false;
		final Object this$updatedAt = this.getUpdatedAt();
		final Object other$updatedAt = other.getUpdatedAt();
		if (this$updatedAt == null ? other$updatedAt != null : !this$updatedAt.equals(other$updatedAt)) return false;
		return true;
	}

	protected boolean canEqual(final Object other) {
		return other instanceof WorkflowDefinitionEntity;
	}

	@Override
	public int hashCode() {
		final int PRIME = 59;
		int result = 1;
		result = result * PRIME + this.getVersion();
		result = result * PRIME + (this.isActive() ? 79 : 97);
		final Object $id = this.getId();
		result = result * PRIME + ($id == null ? 43 : $id.hashCode());
		final Object $code = this.getCode();
		result = result * PRIME + ($code == null ? 43 : $code.hashCode());
		final Object $name = this.getName();
		result = result * PRIME + ($name == null ? 43 : $name.hashCode());
		final Object $language = this.getLanguage();
		result = result * PRIME + ($language == null ? 43 : $language.hashCode());
		final Object $dispatchPoolCode = this.getDispatchPoolCode();
		result = result * PRIME + ($dispatchPoolCode == null ? 43 : $dispatchPoolCode.hashCode());
		final Object $createdAt = this.getCreatedAt();
		result = result * PRIME + ($createdAt == null ? 43 : $createdAt.hashCode());
		final Object $updatedAt = this.getUpdatedAt();
		result = result * PRIME + ($updatedAt == null ? 43 : $updatedAt.hashCode());
		return result;
	}

	@Override
	public String toString() {
		return "WorkflowDefinitionEntity(id=" + this.getId() + ", code=" + this.getCode() + ", name=" + this.getName() + ", language=" + this.getLanguage() + ", version=" + this.getVersion() + ", active=" + this.isActive() + ", dispatchPoolCode=" + this.getDispatchPoolCode() + ", createdAt=" + this.getCreatedAt() + ", updatedAt=" + this.getUpdatedAt() + ")";
	}
}
