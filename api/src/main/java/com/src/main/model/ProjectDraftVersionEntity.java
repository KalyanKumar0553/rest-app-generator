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

@Entity
@Table(name = AppDbTables.PROJECT_DRAFT_VERSIONS, indexes = {@Index(name = "idx_project_draft_versions_project_created", columnList = "project_id, created_at DESC"), @Index(name = "idx_project_draft_versions_project_version", columnList = "project_id, draft_version DESC")})
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

	public ProjectDraftVersionEntity() {
	}

	public UUID getId() {
		return this.id;
	}

	public ProjectEntity getProject() {
		return this.project;
	}

	public Integer getDraftVersion() {
		return this.draftVersion;
	}

	public String getDraftData() {
		return this.draftData;
	}

	public String getYaml() {
		return this.yaml;
	}

	public String getGenerator() {
		return this.generator;
	}

	public String getCreatedByUserId() {
		return this.createdByUserId;
	}

	public UUID getRestoredFromVersionId() {
		return this.restoredFromVersionId;
	}

	public OffsetDateTime getCreatedAt() {
		return this.createdAt;
	}

	public void setId(final UUID id) {
		this.id = id;
	}

	public void setProject(final ProjectEntity project) {
		this.project = project;
	}

	public void setDraftVersion(final Integer draftVersion) {
		this.draftVersion = draftVersion;
	}

	public void setDraftData(final String draftData) {
		this.draftData = draftData;
	}

	public void setYaml(final String yaml) {
		this.yaml = yaml;
	}

	public void setGenerator(final String generator) {
		this.generator = generator;
	}

	public void setCreatedByUserId(final String createdByUserId) {
		this.createdByUserId = createdByUserId;
	}

	public void setRestoredFromVersionId(final UUID restoredFromVersionId) {
		this.restoredFromVersionId = restoredFromVersionId;
	}

	public void setCreatedAt(final OffsetDateTime createdAt) {
		this.createdAt = createdAt;
	}

	@Override
	public boolean equals(final Object o) {
		if (o == this) return true;
		if (!(o instanceof ProjectDraftVersionEntity)) return false;
		final ProjectDraftVersionEntity other = (ProjectDraftVersionEntity) o;
		if (!other.canEqual((Object) this)) return false;
		final Object this$draftVersion = this.getDraftVersion();
		final Object other$draftVersion = other.getDraftVersion();
		if (this$draftVersion == null ? other$draftVersion != null : !this$draftVersion.equals(other$draftVersion)) return false;
		final Object this$id = this.getId();
		final Object other$id = other.getId();
		if (this$id == null ? other$id != null : !this$id.equals(other$id)) return false;
		final Object this$project = this.getProject();
		final Object other$project = other.getProject();
		if (this$project == null ? other$project != null : !this$project.equals(other$project)) return false;
		final Object this$draftData = this.getDraftData();
		final Object other$draftData = other.getDraftData();
		if (this$draftData == null ? other$draftData != null : !this$draftData.equals(other$draftData)) return false;
		final Object this$yaml = this.getYaml();
		final Object other$yaml = other.getYaml();
		if (this$yaml == null ? other$yaml != null : !this$yaml.equals(other$yaml)) return false;
		final Object this$generator = this.getGenerator();
		final Object other$generator = other.getGenerator();
		if (this$generator == null ? other$generator != null : !this$generator.equals(other$generator)) return false;
		final Object this$createdByUserId = this.getCreatedByUserId();
		final Object other$createdByUserId = other.getCreatedByUserId();
		if (this$createdByUserId == null ? other$createdByUserId != null : !this$createdByUserId.equals(other$createdByUserId)) return false;
		final Object this$restoredFromVersionId = this.getRestoredFromVersionId();
		final Object other$restoredFromVersionId = other.getRestoredFromVersionId();
		if (this$restoredFromVersionId == null ? other$restoredFromVersionId != null : !this$restoredFromVersionId.equals(other$restoredFromVersionId)) return false;
		final Object this$createdAt = this.getCreatedAt();
		final Object other$createdAt = other.getCreatedAt();
		if (this$createdAt == null ? other$createdAt != null : !this$createdAt.equals(other$createdAt)) return false;
		return true;
	}

	protected boolean canEqual(final Object other) {
		return other instanceof ProjectDraftVersionEntity;
	}

	@Override
	public int hashCode() {
		final int PRIME = 59;
		int result = 1;
		final Object $draftVersion = this.getDraftVersion();
		result = result * PRIME + ($draftVersion == null ? 43 : $draftVersion.hashCode());
		final Object $id = this.getId();
		result = result * PRIME + ($id == null ? 43 : $id.hashCode());
		final Object $project = this.getProject();
		result = result * PRIME + ($project == null ? 43 : $project.hashCode());
		final Object $draftData = this.getDraftData();
		result = result * PRIME + ($draftData == null ? 43 : $draftData.hashCode());
		final Object $yaml = this.getYaml();
		result = result * PRIME + ($yaml == null ? 43 : $yaml.hashCode());
		final Object $generator = this.getGenerator();
		result = result * PRIME + ($generator == null ? 43 : $generator.hashCode());
		final Object $createdByUserId = this.getCreatedByUserId();
		result = result * PRIME + ($createdByUserId == null ? 43 : $createdByUserId.hashCode());
		final Object $restoredFromVersionId = this.getRestoredFromVersionId();
		result = result * PRIME + ($restoredFromVersionId == null ? 43 : $restoredFromVersionId.hashCode());
		final Object $createdAt = this.getCreatedAt();
		result = result * PRIME + ($createdAt == null ? 43 : $createdAt.hashCode());
		return result;
	}

	@Override
	public String toString() {
		return "ProjectDraftVersionEntity(id=" + this.getId() + ", project=" + this.getProject() + ", draftVersion=" + this.getDraftVersion() + ", draftData=" + this.getDraftData() + ", yaml=" + this.getYaml() + ", generator=" + this.getGenerator() + ", createdByUserId=" + this.getCreatedByUserId() + ", restoredFromVersionId=" + this.getRestoredFromVersionId() + ", createdAt=" + this.getCreatedAt() + ")";
	}
}
