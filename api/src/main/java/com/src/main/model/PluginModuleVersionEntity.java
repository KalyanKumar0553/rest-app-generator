package com.src.main.model;

import java.time.OffsetDateTime;
import java.util.UUID;
import com.src.main.config.AppDbTables;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = AppDbTables.PLUGIN_MODULE_VERSIONS)
public class PluginModuleVersionEntity {
	@Id
	private UUID id;
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "plugin_module_id", nullable = false)
	private PluginModuleEntity pluginModule;
	@Column(name = "version_code", nullable = false, length = 64)
	private String versionCode;
	@Column(columnDefinition = "text")
	private String changelog;
	@Column(name = "file_name", nullable = false, length = 255)
	private String fileName;
	@Column(name = "storage_key", nullable = false, length = 500, unique = true)
	private String storageKey;
	@Column(name = "checksum_sha256", nullable = false, length = 128)
	private String checksumSha256;
	@Column(name = "size_bytes", nullable = false)
	private long sizeBytes;
	@Column(nullable = false)
	private boolean published;
	@Column(name = "created_by_user_id", nullable = false, length = 100)
	private String createdByUserId;
	@Column(name = "created_at", nullable = false)
	private OffsetDateTime createdAt;
	@Column(name = "updated_at", nullable = false)
	private OffsetDateTime updatedAt;

	public PluginModuleVersionEntity() {
	}

	public UUID getId() {
		return this.id;
	}

	public PluginModuleEntity getPluginModule() {
		return this.pluginModule;
	}

	public String getVersionCode() {
		return this.versionCode;
	}

	public String getChangelog() {
		return this.changelog;
	}

	public String getFileName() {
		return this.fileName;
	}

	public String getStorageKey() {
		return this.storageKey;
	}

	public String getChecksumSha256() {
		return this.checksumSha256;
	}

	public long getSizeBytes() {
		return this.sizeBytes;
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

	public void setPluginModule(final PluginModuleEntity pluginModule) {
		this.pluginModule = pluginModule;
	}

	public void setVersionCode(final String versionCode) {
		this.versionCode = versionCode;
	}

	public void setChangelog(final String changelog) {
		this.changelog = changelog;
	}

	public void setFileName(final String fileName) {
		this.fileName = fileName;
	}

	public void setStorageKey(final String storageKey) {
		this.storageKey = storageKey;
	}

	public void setChecksumSha256(final String checksumSha256) {
		this.checksumSha256 = checksumSha256;
	}

	public void setSizeBytes(final long sizeBytes) {
		this.sizeBytes = sizeBytes;
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
		if (!(o instanceof PluginModuleVersionEntity)) return false;
		final PluginModuleVersionEntity other = (PluginModuleVersionEntity) o;
		if (!other.canEqual((Object) this)) return false;
		if (this.getSizeBytes() != other.getSizeBytes()) return false;
		if (this.isPublished() != other.isPublished()) return false;
		final Object this$id = this.getId();
		final Object other$id = other.getId();
		if (this$id == null ? other$id != null : !this$id.equals(other$id)) return false;
		final Object this$versionCode = this.getVersionCode();
		final Object other$versionCode = other.getVersionCode();
		if (this$versionCode == null ? other$versionCode != null : !this$versionCode.equals(other$versionCode)) return false;
		final Object this$changelog = this.getChangelog();
		final Object other$changelog = other.getChangelog();
		if (this$changelog == null ? other$changelog != null : !this$changelog.equals(other$changelog)) return false;
		final Object this$fileName = this.getFileName();
		final Object other$fileName = other.getFileName();
		if (this$fileName == null ? other$fileName != null : !this$fileName.equals(other$fileName)) return false;
		final Object this$storageKey = this.getStorageKey();
		final Object other$storageKey = other.getStorageKey();
		if (this$storageKey == null ? other$storageKey != null : !this$storageKey.equals(other$storageKey)) return false;
		final Object this$checksumSha256 = this.getChecksumSha256();
		final Object other$checksumSha256 = other.getChecksumSha256();
		if (this$checksumSha256 == null ? other$checksumSha256 != null : !this$checksumSha256.equals(other$checksumSha256)) return false;
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
		return other instanceof PluginModuleVersionEntity;
	}

	@Override
	public int hashCode() {
		final int PRIME = 59;
		int result = 1;
		final long $sizeBytes = this.getSizeBytes();
		result = result * PRIME + (int) ($sizeBytes >>> 32 ^ $sizeBytes);
		result = result * PRIME + (this.isPublished() ? 79 : 97);
		final Object $id = this.getId();
		result = result * PRIME + ($id == null ? 43 : $id.hashCode());
		final Object $versionCode = this.getVersionCode();
		result = result * PRIME + ($versionCode == null ? 43 : $versionCode.hashCode());
		final Object $changelog = this.getChangelog();
		result = result * PRIME + ($changelog == null ? 43 : $changelog.hashCode());
		final Object $fileName = this.getFileName();
		result = result * PRIME + ($fileName == null ? 43 : $fileName.hashCode());
		final Object $storageKey = this.getStorageKey();
		result = result * PRIME + ($storageKey == null ? 43 : $storageKey.hashCode());
		final Object $checksumSha256 = this.getChecksumSha256();
		result = result * PRIME + ($checksumSha256 == null ? 43 : $checksumSha256.hashCode());
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
		return "PluginModuleVersionEntity(id=" + this.getId() + ", versionCode=" + this.getVersionCode() + ", changelog=" + this.getChangelog() + ", fileName=" + this.getFileName() + ", storageKey=" + this.getStorageKey() + ", checksumSha256=" + this.getChecksumSha256() + ", sizeBytes=" + this.getSizeBytes() + ", published=" + this.isPublished() + ", createdByUserId=" + this.getCreatedByUserId() + ", createdAt=" + this.getCreatedAt() + ", updatedAt=" + this.getUpdatedAt() + ")";
	}
}
