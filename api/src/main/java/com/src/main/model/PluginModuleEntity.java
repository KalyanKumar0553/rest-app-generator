package com.src.main.model;

import java.time.OffsetDateTime;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import com.src.main.config.AppDbTables;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = AppDbTables.PLUGIN_MODULES)
public class PluginModuleEntity {
	@Id
	private UUID id;
	@Column(nullable = false, length = 100, unique = true)
	private String code;
	@Column(nullable = false, length = 150)
	private String name;
	@JdbcTypeCode(SqlTypes.LONGVARCHAR)
	@Column(columnDefinition = "text")
	private String description;
	@Column(length = 100)
	private String category;
	@Column(nullable = false)
	private boolean enabled;
	@Column(name = "enable_config", nullable = false)
	private boolean enableConfig;
	@JdbcTypeCode(SqlTypes.LONGVARCHAR)
	@Column(name = "generator_targets_json", nullable = false, columnDefinition = "text")
	private String generatorTargetsJson;
	@Column(name = "current_published_version_id")
	private UUID currentPublishedVersionId;
	@Column(name = "created_by_user_id", nullable = false, length = 100)
	private String createdByUserId;
	@Column(name = "created_at", nullable = false)
	private OffsetDateTime createdAt;
	@Column(name = "updated_at", nullable = false)
	private OffsetDateTime updatedAt;

	public PluginModuleEntity() {
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

	public String getDescription() {
		return this.description;
	}

	public String getCategory() {
		return this.category;
	}

	public boolean isEnabled() {
		return this.enabled;
	}

	public boolean isEnableConfig() {
		return this.enableConfig;
	}

	public String getGeneratorTargetsJson() {
		return this.generatorTargetsJson;
	}

	public UUID getCurrentPublishedVersionId() {
		return this.currentPublishedVersionId;
	}

	public String getCreatedByUserId() {
		return this.createdByUserId;
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

	public void setDescription(final String description) {
		this.description = description;
	}

	public void setCategory(final String category) {
		this.category = category;
	}

	public void setEnabled(final boolean enabled) {
		this.enabled = enabled;
	}

	public void setEnableConfig(final boolean enableConfig) {
		this.enableConfig = enableConfig;
	}

	public void setGeneratorTargetsJson(final String generatorTargetsJson) {
		this.generatorTargetsJson = generatorTargetsJson;
	}

	public void setCurrentPublishedVersionId(final UUID currentPublishedVersionId) {
		this.currentPublishedVersionId = currentPublishedVersionId;
	}

	public void setCreatedByUserId(final String createdByUserId) {
		this.createdByUserId = createdByUserId;
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
		if (!(o instanceof PluginModuleEntity)) return false;
		final PluginModuleEntity other = (PluginModuleEntity) o;
		if (!other.canEqual((Object) this)) return false;
		if (this.isEnabled() != other.isEnabled()) return false;
		if (this.isEnableConfig() != other.isEnableConfig()) return false;
		final Object this$id = this.getId();
		final Object other$id = other.getId();
		if (this$id == null ? other$id != null : !this$id.equals(other$id)) return false;
		final Object this$code = this.getCode();
		final Object other$code = other.getCode();
		if (this$code == null ? other$code != null : !this$code.equals(other$code)) return false;
		final Object this$name = this.getName();
		final Object other$name = other.getName();
		if (this$name == null ? other$name != null : !this$name.equals(other$name)) return false;
		final Object this$description = this.getDescription();
		final Object other$description = other.getDescription();
		if (this$description == null ? other$description != null : !this$description.equals(other$description)) return false;
		final Object this$category = this.getCategory();
		final Object other$category = other.getCategory();
		if (this$category == null ? other$category != null : !this$category.equals(other$category)) return false;
		final Object this$generatorTargetsJson = this.getGeneratorTargetsJson();
		final Object other$generatorTargetsJson = other.getGeneratorTargetsJson();
		if (this$generatorTargetsJson == null ? other$generatorTargetsJson != null : !this$generatorTargetsJson.equals(other$generatorTargetsJson)) return false;
		final Object this$currentPublishedVersionId = this.getCurrentPublishedVersionId();
		final Object other$currentPublishedVersionId = other.getCurrentPublishedVersionId();
		if (this$currentPublishedVersionId == null ? other$currentPublishedVersionId != null : !this$currentPublishedVersionId.equals(other$currentPublishedVersionId)) return false;
		final Object this$createdByUserId = this.getCreatedByUserId();
		final Object other$createdByUserId = other.getCreatedByUserId();
		if (this$createdByUserId == null ? other$createdByUserId != null : !this$createdByUserId.equals(other$createdByUserId)) return false;
		final Object this$createdAt = this.getCreatedAt();
		final Object other$createdAt = other.getCreatedAt();
		if (this$createdAt == null ? other$createdAt != null : !this$createdAt.equals(other$createdAt)) return false;
		final Object this$updatedAt = this.getUpdatedAt();
		final Object other$updatedAt = other.getUpdatedAt();
		if (this$updatedAt == null ? other$updatedAt != null : !this$updatedAt.equals(other$updatedAt)) return false;
		return true;
	}

	protected boolean canEqual(final Object other) {
		return other instanceof PluginModuleEntity;
	}

	@Override
	public int hashCode() {
		final int PRIME = 59;
		int result = 1;
		result = result * PRIME + (this.isEnabled() ? 79 : 97);
		result = result * PRIME + (this.isEnableConfig() ? 79 : 97);
		final Object $id = this.getId();
		result = result * PRIME + ($id == null ? 43 : $id.hashCode());
		final Object $code = this.getCode();
		result = result * PRIME + ($code == null ? 43 : $code.hashCode());
		final Object $name = this.getName();
		result = result * PRIME + ($name == null ? 43 : $name.hashCode());
		final Object $description = this.getDescription();
		result = result * PRIME + ($description == null ? 43 : $description.hashCode());
		final Object $category = this.getCategory();
		result = result * PRIME + ($category == null ? 43 : $category.hashCode());
		final Object $generatorTargetsJson = this.getGeneratorTargetsJson();
		result = result * PRIME + ($generatorTargetsJson == null ? 43 : $generatorTargetsJson.hashCode());
		final Object $currentPublishedVersionId = this.getCurrentPublishedVersionId();
		result = result * PRIME + ($currentPublishedVersionId == null ? 43 : $currentPublishedVersionId.hashCode());
		final Object $createdByUserId = this.getCreatedByUserId();
		result = result * PRIME + ($createdByUserId == null ? 43 : $createdByUserId.hashCode());
		final Object $createdAt = this.getCreatedAt();
		result = result * PRIME + ($createdAt == null ? 43 : $createdAt.hashCode());
		final Object $updatedAt = this.getUpdatedAt();
		result = result * PRIME + ($updatedAt == null ? 43 : $updatedAt.hashCode());
		return result;
	}

	@Override
	public String toString() {
		return "PluginModuleEntity(id=" + this.getId() + ", code=" + this.getCode() + ", name=" + this.getName() + ", description=" + this.getDescription() + ", category=" + this.getCategory() + ", enabled=" + this.isEnabled() + ", enableConfig=" + this.isEnableConfig() + ", generatorTargetsJson=" + this.getGeneratorTargetsJson() + ", currentPublishedVersionId=" + this.getCurrentPublishedVersionId() + ", createdByUserId=" + this.getCreatedByUserId() + ", createdAt=" + this.getCreatedAt() + ", updatedAt=" + this.getUpdatedAt() + ")";
	}
}
