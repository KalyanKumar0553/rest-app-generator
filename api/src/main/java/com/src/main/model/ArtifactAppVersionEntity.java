package com.src.main.model;

import java.time.OffsetDateTime;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import com.src.main.config.AppDbTables;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = AppDbTables.ARTIFACT_APP_VERSIONS)
public class ArtifactAppVersionEntity {
	@Id
	private UUID id;
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "app_id", nullable = false)
	private ArtifactAppEntity app;
	@Column(name = "version_code", nullable = false, length = 64)
	private String versionCode;
	@JdbcTypeCode(SqlTypes.LONGVARCHAR)
	@Column(name = "config_json", nullable = false, columnDefinition = "text")
	private String configJson;
	@Column(name = "published", nullable = false)
	private boolean published;
	@Column(name = "created_by_user_id", nullable = false, length = 100)
	private String createdByUserId;
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

	public ArtifactAppVersionEntity() {
	}

	public UUID getId() {
		return this.id;
	}

	public ArtifactAppEntity getApp() {
		return this.app;
	}

	public String getVersionCode() {
		return this.versionCode;
	}

	public String getConfigJson() {
		return this.configJson;
	}

	public boolean isPublished() {
		return this.published;
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

	public void setApp(final ArtifactAppEntity app) {
		this.app = app;
	}

	public void setVersionCode(final String versionCode) {
		this.versionCode = versionCode;
	}

	public void setConfigJson(final String configJson) {
		this.configJson = configJson;
	}

	public void setPublished(final boolean published) {
		this.published = published;
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
		if (!(o instanceof ArtifactAppVersionEntity)) return false;
		final ArtifactAppVersionEntity other = (ArtifactAppVersionEntity) o;
		if (!other.canEqual((Object) this)) return false;
		if (this.isPublished() != other.isPublished()) return false;
		final Object this$id = this.getId();
		final Object other$id = other.getId();
		if (this$id == null ? other$id != null : !this$id.equals(other$id)) return false;
		final Object this$app = this.getApp();
		final Object other$app = other.getApp();
		if (this$app == null ? other$app != null : !this$app.equals(other$app)) return false;
		final Object this$versionCode = this.getVersionCode();
		final Object other$versionCode = other.getVersionCode();
		if (this$versionCode == null ? other$versionCode != null : !this$versionCode.equals(other$versionCode)) return false;
		final Object this$configJson = this.getConfigJson();
		final Object other$configJson = other.getConfigJson();
		if (this$configJson == null ? other$configJson != null : !this$configJson.equals(other$configJson)) return false;
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
		return other instanceof ArtifactAppVersionEntity;
	}

	@Override
	public int hashCode() {
		final int PRIME = 59;
		int result = 1;
		result = result * PRIME + (this.isPublished() ? 79 : 97);
		final Object $id = this.getId();
		result = result * PRIME + ($id == null ? 43 : $id.hashCode());
		final Object $app = this.getApp();
		result = result * PRIME + ($app == null ? 43 : $app.hashCode());
		final Object $versionCode = this.getVersionCode();
		result = result * PRIME + ($versionCode == null ? 43 : $versionCode.hashCode());
		final Object $configJson = this.getConfigJson();
		result = result * PRIME + ($configJson == null ? 43 : $configJson.hashCode());
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
		return "ArtifactAppVersionEntity(id=" + this.getId() + ", app=" + this.getApp() + ", versionCode=" + this.getVersionCode() + ", configJson=" + this.getConfigJson() + ", published=" + this.isPublished() + ", createdByUserId=" + this.getCreatedByUserId() + ", createdAt=" + this.getCreatedAt() + ", updatedAt=" + this.getUpdatedAt() + ")";
	}
}
