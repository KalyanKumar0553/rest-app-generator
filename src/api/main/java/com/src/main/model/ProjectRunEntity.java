package com.src.main.model;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.src.main.util.ProjectRunStatus;
import com.src.main.util.ProjectRunType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "project_runs", indexes = {
		@Index(name = "idx_runs_owner_type_created", columnList = "owner_id, type, created_at"),
		@Index(name = "idx_runs_project", columnList = "project_id") })
@Data
public class ProjectRunEntity {

	@Id
	@GeneratedValue
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "project_id", nullable = false)
	private ProjectEntity project;

	@Column(name = "owner_id", nullable = false, length = 100)
	private String ownerId;

	@Enumerated(EnumType.STRING)
	@Column(name = "type", nullable = false)
	private ProjectRunType type;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false)
	private ProjectRunStatus status;

	@Column(name = "run_number", nullable = false)
	private int runNumber;

	@Column(name = "error_message")
	private String errorMessage;

	@Column(name = "created_at", nullable = false, updatable = false)
	private OffsetDateTime createdAt;

	@Column(name = "updated_at")
	private OffsetDateTime updatedAt;


	@JdbcTypeCode(SqlTypes.LONGVARBINARY)
	@Column(columnDefinition = "bytea")
	private byte[] zip;

	@PrePersist
	public void onCreate() {
		this.createdAt = OffsetDateTime.now();
		this.updatedAt = this.createdAt;
	}

	@PreUpdate
	public void onUpdate() {
		this.updatedAt = OffsetDateTime.now();
	}
}
