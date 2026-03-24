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
import lombok.Data;

@Entity
@Table(name = AppDbTables.ARTIFACT_APPS)
@Data
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
}
