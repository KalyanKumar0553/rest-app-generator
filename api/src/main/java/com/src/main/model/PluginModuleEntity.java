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
import lombok.Data;

@Entity
@Table(name = AppDbTables.PLUGIN_MODULES)
@Data
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
}
