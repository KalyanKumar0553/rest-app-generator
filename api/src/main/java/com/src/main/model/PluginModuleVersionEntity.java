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
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Entity
@Table(name = AppDbTables.PLUGIN_MODULE_VERSIONS)
@Data
public class PluginModuleVersionEntity {

	@Id
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "plugin_module_id", nullable = false)
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
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
}
