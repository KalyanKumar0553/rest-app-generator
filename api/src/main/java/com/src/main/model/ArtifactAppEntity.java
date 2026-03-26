package com.src.main.model;

import java.time.OffsetDateTime;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import com.src.main.config.AppDbTables;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = AppDbTables.ARTIFACT_APPS)
public class ArtifactAppEntity {
	@Id
	private UUID id;
	@Column(name = "code", nullable = false, length = 100, unique = true)
	private String code;
	@Column(name = "name", nullable = false, length = 150)
	private String name;
	@JdbcTypeCode(SqlTypes.LONGVARCHAR)
	@Column(name = "description", columnDefinition = "text")
	private String description;
	@Column(name = "status", nullable = false, length = 32)
	private String status;
	@Column(name = "owner_user_id", nullable = false, length = 100)
	private String ownerUserId;
	@Column(name = "generator_language", nullable = false, length = 32)
	private String generatorLanguage;
	@Column(name = "build_tool", nullable = false, length = 32)
	private String buildTool;
	@JdbcTypeCode(SqlTypes.LONGVARCHAR)
	@Column(name = "enabled_packs_json", nullable = false, columnDefinition = "text")
	private String enabledPacksJson;
	@JdbcTypeCode(SqlTypes.LONGVARCHAR)
	@Column(name = "config_json", nullable = false, columnDefinition = "text")
	private String configJson;
	@Column(name = "published_version", length = 64)
	private String publishedVersion;
	@Column(name = "created_at", nullable = false, updatable = false)
	private OffsetDateTime createdAt;
	@Column(name = "updated_at", nullable = false)
	private OffsetDateTime updatedAt;

	@PrePersist
	public void prePersist() {
		if (id == null) {
			id = UUID.randomUUID();
		}
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

	public ArtifactAppEntity() {
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

	public String getStatus() {
		return this.status;
	}

	public String getOwnerUserId() {
		return this.ownerUserId;
	}

	public String getGeneratorLanguage() {
		return this.generatorLanguage;
	}

	public String getBuildTool() {
		return this.buildTool;
	}

	public String getEnabledPacksJson() {
		return this.enabledPacksJson;
	}

	public String getConfigJson() {
		return this.configJson;
	}

	public String getPublishedVersion() {
		return this.publishedVersion;
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

	public void setStatus(final String status) {
		this.status = status;
	}

	public void setOwnerUserId(final String ownerUserId) {
		this.ownerUserId = ownerUserId;
	}

	public void setGeneratorLanguage(final String generatorLanguage) {
		this.generatorLanguage = generatorLanguage;
	}

	public void setBuildTool(final String buildTool) {
		this.buildTool = buildTool;
	}

	public void setEnabledPacksJson(final String enabledPacksJson) {
		this.enabledPacksJson = enabledPacksJson;
	}

	public void setConfigJson(final String configJson) {
		this.configJson = configJson;
	}

	public void setPublishedVersion(final String publishedVersion) {
		this.publishedVersion = publishedVersion;
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
		if (!(o instanceof ArtifactAppEntity)) return false;
		final ArtifactAppEntity other = (ArtifactAppEntity) o;
		if (!other.canEqual((Object) this)) return false;
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
		final Object this$status = this.getStatus();
		final Object other$status = other.getStatus();
		if (this$status == null ? other$status != null : !this$status.equals(other$status)) return false;
		final Object this$ownerUserId = this.getOwnerUserId();
		final Object other$ownerUserId = other.getOwnerUserId();
		if (this$ownerUserId == null ? other$ownerUserId != null : !this$ownerUserId.equals(other$ownerUserId)) return false;
		final Object this$generatorLanguage = this.getGeneratorLanguage();
		final Object other$generatorLanguage = other.getGeneratorLanguage();
		if (this$generatorLanguage == null ? other$generatorLanguage != null : !this$generatorLanguage.equals(other$generatorLanguage)) return false;
		final Object this$buildTool = this.getBuildTool();
		final Object other$buildTool = other.getBuildTool();
		if (this$buildTool == null ? other$buildTool != null : !this$buildTool.equals(other$buildTool)) return false;
		final Object this$enabledPacksJson = this.getEnabledPacksJson();
		final Object other$enabledPacksJson = other.getEnabledPacksJson();
		if (this$enabledPacksJson == null ? other$enabledPacksJson != null : !this$enabledPacksJson.equals(other$enabledPacksJson)) return false;
		final Object this$configJson = this.getConfigJson();
		final Object other$configJson = other.getConfigJson();
		if (this$configJson == null ? other$configJson != null : !this$configJson.equals(other$configJson)) return false;
		final Object this$publishedVersion = this.getPublishedVersion();
		final Object other$publishedVersion = other.getPublishedVersion();
		if (this$publishedVersion == null ? other$publishedVersion != null : !this$publishedVersion.equals(other$publishedVersion)) return false;
		final Object this$createdAt = this.getCreatedAt();
		final Object other$createdAt = other.getCreatedAt();
		if (this$createdAt == null ? other$createdAt != null : !this$createdAt.equals(other$createdAt)) return false;
		final Object this$updatedAt = this.getUpdatedAt();
		final Object other$updatedAt = other.getUpdatedAt();
		if (this$updatedAt == null ? other$updatedAt != null : !this$updatedAt.equals(other$updatedAt)) return false;
		return true;
	}

	protected boolean canEqual(final Object other) {
		return other instanceof ArtifactAppEntity;
	}

	@Override
	public int hashCode() {
		final int PRIME = 59;
		int result = 1;
		final Object $id = this.getId();
		result = result * PRIME + ($id == null ? 43 : $id.hashCode());
		final Object $code = this.getCode();
		result = result * PRIME + ($code == null ? 43 : $code.hashCode());
		final Object $name = this.getName();
		result = result * PRIME + ($name == null ? 43 : $name.hashCode());
		final Object $description = this.getDescription();
		result = result * PRIME + ($description == null ? 43 : $description.hashCode());
		final Object $status = this.getStatus();
		result = result * PRIME + ($status == null ? 43 : $status.hashCode());
		final Object $ownerUserId = this.getOwnerUserId();
		result = result * PRIME + ($ownerUserId == null ? 43 : $ownerUserId.hashCode());
		final Object $generatorLanguage = this.getGeneratorLanguage();
		result = result * PRIME + ($generatorLanguage == null ? 43 : $generatorLanguage.hashCode());
		final Object $buildTool = this.getBuildTool();
		result = result * PRIME + ($buildTool == null ? 43 : $buildTool.hashCode());
		final Object $enabledPacksJson = this.getEnabledPacksJson();
		result = result * PRIME + ($enabledPacksJson == null ? 43 : $enabledPacksJson.hashCode());
		final Object $configJson = this.getConfigJson();
		result = result * PRIME + ($configJson == null ? 43 : $configJson.hashCode());
		final Object $publishedVersion = this.getPublishedVersion();
		result = result * PRIME + ($publishedVersion == null ? 43 : $publishedVersion.hashCode());
		final Object $createdAt = this.getCreatedAt();
		result = result * PRIME + ($createdAt == null ? 43 : $createdAt.hashCode());
		final Object $updatedAt = this.getUpdatedAt();
		result = result * PRIME + ($updatedAt == null ? 43 : $updatedAt.hashCode());
		return result;
	}

	@Override
	public String toString() {
		return "ArtifactAppEntity(id=" + this.getId() + ", code=" + this.getCode() + ", name=" + this.getName() + ", description=" + this.getDescription() + ", status=" + this.getStatus() + ", ownerUserId=" + this.getOwnerUserId() + ", generatorLanguage=" + this.getGeneratorLanguage() + ", buildTool=" + this.getBuildTool() + ", enabledPacksJson=" + this.getEnabledPacksJson() + ", configJson=" + this.getConfigJson() + ", publishedVersion=" + this.getPublishedVersion() + ", createdAt=" + this.getCreatedAt() + ", updatedAt=" + this.getUpdatedAt() + ")";
	}
}
