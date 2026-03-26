package com.src.main.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public class ProjectSummaryDTO {
	private String projectId;
	private String artifact;
	private UUID id;
	private String name;
	private String description;
	private String generator;
	private OffsetDateTime createdAt;
	private OffsetDateTime updatedAt;
	private String ownerId;
	private boolean contributorAccess;

	public String getProjectId() {
		return this.projectId;
	}

	public String getArtifact() {
		return this.artifact;
	}

	public UUID getId() {
		return this.id;
	}

	public String getName() {
		return this.name;
	}

	public String getDescription() {
		return this.description;
	}

	public String getGenerator() {
		return this.generator;
	}

	public OffsetDateTime getCreatedAt() {
		return this.createdAt;
	}

	public OffsetDateTime getUpdatedAt() {
		return this.updatedAt;
	}

	public String getOwnerId() {
		return this.ownerId;
	}

	public boolean isContributorAccess() {
		return this.contributorAccess;
	}

	public void setProjectId(final String projectId) {
		this.projectId = projectId;
	}

	public void setArtifact(final String artifact) {
		this.artifact = artifact;
	}

	public void setId(final UUID id) {
		this.id = id;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public void setDescription(final String description) {
		this.description = description;
	}

	public void setGenerator(final String generator) {
		this.generator = generator;
	}

	public void setCreatedAt(final OffsetDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public void setUpdatedAt(final OffsetDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}

	public void setOwnerId(final String ownerId) {
		this.ownerId = ownerId;
	}

	public void setContributorAccess(final boolean contributorAccess) {
		this.contributorAccess = contributorAccess;
	}

	@Override
	public boolean equals(final Object o) {
		if (o == this) return true;
		if (!(o instanceof ProjectSummaryDTO)) return false;
		final ProjectSummaryDTO other = (ProjectSummaryDTO) o;
		if (!other.canEqual((Object) this)) return false;
		if (this.isContributorAccess() != other.isContributorAccess()) return false;
		final Object this$projectId = this.getProjectId();
		final Object other$projectId = other.getProjectId();
		if (this$projectId == null ? other$projectId != null : !this$projectId.equals(other$projectId)) return false;
		final Object this$artifact = this.getArtifact();
		final Object other$artifact = other.getArtifact();
		if (this$artifact == null ? other$artifact != null : !this$artifact.equals(other$artifact)) return false;
		final Object this$id = this.getId();
		final Object other$id = other.getId();
		if (this$id == null ? other$id != null : !this$id.equals(other$id)) return false;
		final Object this$name = this.getName();
		final Object other$name = other.getName();
		if (this$name == null ? other$name != null : !this$name.equals(other$name)) return false;
		final Object this$description = this.getDescription();
		final Object other$description = other.getDescription();
		if (this$description == null ? other$description != null : !this$description.equals(other$description)) return false;
		final Object this$generator = this.getGenerator();
		final Object other$generator = other.getGenerator();
		if (this$generator == null ? other$generator != null : !this$generator.equals(other$generator)) return false;
		final Object this$createdAt = this.getCreatedAt();
		final Object other$createdAt = other.getCreatedAt();
		if (this$createdAt == null ? other$createdAt != null : !this$createdAt.equals(other$createdAt)) return false;
		final Object this$updatedAt = this.getUpdatedAt();
		final Object other$updatedAt = other.getUpdatedAt();
		if (this$updatedAt == null ? other$updatedAt != null : !this$updatedAt.equals(other$updatedAt)) return false;
		final Object this$ownerId = this.getOwnerId();
		final Object other$ownerId = other.getOwnerId();
		if (this$ownerId == null ? other$ownerId != null : !this$ownerId.equals(other$ownerId)) return false;
		return true;
	}

	protected boolean canEqual(final Object other) {
		return other instanceof ProjectSummaryDTO;
	}

	@Override
	public int hashCode() {
		final int PRIME = 59;
		int result = 1;
		result = result * PRIME + (this.isContributorAccess() ? 79 : 97);
		final Object $projectId = this.getProjectId();
		result = result * PRIME + ($projectId == null ? 43 : $projectId.hashCode());
		final Object $artifact = this.getArtifact();
		result = result * PRIME + ($artifact == null ? 43 : $artifact.hashCode());
		final Object $id = this.getId();
		result = result * PRIME + ($id == null ? 43 : $id.hashCode());
		final Object $name = this.getName();
		result = result * PRIME + ($name == null ? 43 : $name.hashCode());
		final Object $description = this.getDescription();
		result = result * PRIME + ($description == null ? 43 : $description.hashCode());
		final Object $generator = this.getGenerator();
		result = result * PRIME + ($generator == null ? 43 : $generator.hashCode());
		final Object $createdAt = this.getCreatedAt();
		result = result * PRIME + ($createdAt == null ? 43 : $createdAt.hashCode());
		final Object $updatedAt = this.getUpdatedAt();
		result = result * PRIME + ($updatedAt == null ? 43 : $updatedAt.hashCode());
		final Object $ownerId = this.getOwnerId();
		result = result * PRIME + ($ownerId == null ? 43 : $ownerId.hashCode());
		return result;
	}

	@Override
	public String toString() {
		return "ProjectSummaryDTO(projectId=" + this.getProjectId() + ", artifact=" + this.getArtifact() + ", id=" + this.getId() + ", name=" + this.getName() + ", description=" + this.getDescription() + ", generator=" + this.getGenerator() + ", createdAt=" + this.getCreatedAt() + ", updatedAt=" + this.getUpdatedAt() + ", ownerId=" + this.getOwnerId() + ", contributorAccess=" + this.isContributorAccess() + ")";
	}

	public ProjectSummaryDTO() {
	}

	public ProjectSummaryDTO(final String projectId, final String artifact, final UUID id, final String name, final String description, final String generator, final OffsetDateTime createdAt, final OffsetDateTime updatedAt, final String ownerId, final boolean contributorAccess) {
		this.projectId = projectId;
		this.artifact = artifact;
		this.id = id;
		this.name = name;
		this.description = description;
		this.generator = generator;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
		this.ownerId = ownerId;
		this.contributorAccess = contributorAccess;
	}
}
