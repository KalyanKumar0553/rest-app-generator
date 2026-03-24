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
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Entity
@Table(name = AppDbTables.PROJECT_COLLABORATION_REQUESTS)
@Data
public class ProjectCollaborationRequestEntity {
	@Id
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "project_id", nullable = false)
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	private ProjectEntity project;

	@Column(name = "requester_id", nullable = false, length = 100)
	private String requesterId;

	@Column(name = "status", nullable = false, length = 32)
	private String status;

	@Column(name = "requested_can_edit_draft", nullable = false)
	private boolean requestedCanEditDraft;

	@Column(name = "requested_can_generate", nullable = false)
	private boolean requestedCanGenerate;

	@Column(name = "requested_can_manage_collaboration", nullable = false)
	private boolean requestedCanManageCollaboration;

	@Column(name = "granted_can_edit_draft", nullable = false)
	private boolean grantedCanEditDraft;

	@Column(name = "granted_can_generate", nullable = false)
	private boolean grantedCanGenerate;

	@Column(name = "granted_can_manage_collaboration", nullable = false)
	private boolean grantedCanManageCollaboration;

	@Column(name = "reviewed_by", length = 100)
	private String reviewedBy;

	@Column(name = "reviewed_at")
	private OffsetDateTime reviewedAt;

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
