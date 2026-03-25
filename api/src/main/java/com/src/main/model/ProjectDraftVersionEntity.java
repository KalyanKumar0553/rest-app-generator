package com.src.main.model;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

import com.src.main.config.AppDbTables;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = AppDbTables.PROJECT_DRAFT_VERSIONS, indexes = {
		@Index(name = "idx_project_draft_versions_project_created", columnList = "project_id, created_at DESC"),
		@Index(name = "idx_project_draft_versions_project_version", columnList = "project_id, draft_version DESC") })
@Data
public class ProjectDraftVersionEntity {

	@Id
	@UuidGenerator
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "project_id", nullable = false)
	private ProjectEntity project;

	@Column(name = "draft_version", nullable = false)
	private Integer draftVersion;

	@JdbcTypeCode(SqlTypes.LONGVARCHAR)
	@Column(name = "draft_data", nullable = false, columnDefinition = "text")
	private String draftData;

	@JdbcTypeCode(SqlTypes.LONGVARCHAR)
	@Column(name = "yaml", nullable = false, columnDefinition = "text")
	private String yaml;

	@Column(name = "generator", length = 50)
	private String generator;

	@Column(name = "created_by_user_id", nullable = false, length = 100)
	private String createdByUserId;

	@Column(name = "restored_from_version_id")
	private UUID restoredFromVersionId;

	@Column(name = "created_at", nullable = false)
	private OffsetDateTime createdAt;
}
