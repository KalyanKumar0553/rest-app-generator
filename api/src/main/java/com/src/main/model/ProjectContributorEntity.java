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
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Entity
@Table(
		name = AppDbTables.PROJECT_CONTRIBUTORS,
		uniqueConstraints = @UniqueConstraint(name = "uq_project_contributors_project_user", columnNames = { "project_id", "user_id" }))
@Data
public class ProjectContributorEntity {
	@Id
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "project_id", nullable = false)
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	private ProjectEntity project;

	@Column(name = "user_id", nullable = false, length = 100)
	private String userId;

	@Column(name = "created_at", nullable = false, updatable = false)
	private OffsetDateTime createdAt;

	@Column(name = "can_edit_draft", nullable = false)
	private boolean canEditDraft;

	@Column(name = "can_generate", nullable = false)
	private boolean canGenerate;

	@Column(name = "can_manage_collaboration", nullable = false)
	private boolean canManageCollaboration;

	@PrePersist
	public void prePersist() {
		if (id == null) {
			id = UUID.randomUUID();
		}
		if (createdAt == null) {
			createdAt = OffsetDateTime.now();
		}
	}
}
