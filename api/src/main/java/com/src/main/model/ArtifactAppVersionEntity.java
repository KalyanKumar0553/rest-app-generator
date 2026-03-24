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
import lombok.Data;

@Entity
@Table(name = AppDbTables.ARTIFACT_APP_VERSIONS)
@Data
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
}
