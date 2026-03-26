package com.src.main.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public class ArchivedProjectCollaborationDTO {
	private UUID contributorId;
	private UUID projectId;
	private String projectName;
	private String ownerId;
	private String generator;
	private String inviteToken;
	private OffsetDateTime disabledAt;

	public UUID getContributorId() {
		return this.contributorId;
	}

	public UUID getProjectId() {
		return this.projectId;
	}

	public String getProjectName() {
		return this.projectName;
	}

	public String getOwnerId() {
		return this.ownerId;
	}

	public String getGenerator() {
		return this.generator;
	}

	public String getInviteToken() {
		return this.inviteToken;
	}

	public OffsetDateTime getDisabledAt() {
		return this.disabledAt;
	}

	public void setContributorId(final UUID contributorId) {
		this.contributorId = contributorId;
	}

	public void setProjectId(final UUID projectId) {
		this.projectId = projectId;
	}

	public void setProjectName(final String projectName) {
		this.projectName = projectName;
	}

	public void setOwnerId(final String ownerId) {
		this.ownerId = ownerId;
	}

	public void setGenerator(final String generator) {
		this.generator = generator;
	}

	public void setInviteToken(final String inviteToken) {
		this.inviteToken = inviteToken;
	}

	public void setDisabledAt(final OffsetDateTime disabledAt) {
		this.disabledAt = disabledAt;
	}

	@Override
	public boolean equals(final Object o) {
		if (o == this) return true;
		if (!(o instanceof ArchivedProjectCollaborationDTO)) return false;
		final ArchivedProjectCollaborationDTO other = (ArchivedProjectCollaborationDTO) o;
		if (!other.canEqual((Object) this)) return false;
		final Object this$contributorId = this.getContributorId();
		final Object other$contributorId = other.getContributorId();
		if (this$contributorId == null ? other$contributorId != null : !this$contributorId.equals(other$contributorId)) return false;
		final Object this$projectId = this.getProjectId();
		final Object other$projectId = other.getProjectId();
		if (this$projectId == null ? other$projectId != null : !this$projectId.equals(other$projectId)) return false;
		final Object this$projectName = this.getProjectName();
		final Object other$projectName = other.getProjectName();
		if (this$projectName == null ? other$projectName != null : !this$projectName.equals(other$projectName)) return false;
		final Object this$ownerId = this.getOwnerId();
		final Object other$ownerId = other.getOwnerId();
		if (this$ownerId == null ? other$ownerId != null : !this$ownerId.equals(other$ownerId)) return false;
		final Object this$generator = this.getGenerator();
		final Object other$generator = other.getGenerator();
		if (this$generator == null ? other$generator != null : !this$generator.equals(other$generator)) return false;
		final Object this$inviteToken = this.getInviteToken();
		final Object other$inviteToken = other.getInviteToken();
		if (this$inviteToken == null ? other$inviteToken != null : !this$inviteToken.equals(other$inviteToken)) return false;
		final Object this$disabledAt = this.getDisabledAt();
		final Object other$disabledAt = other.getDisabledAt();
		if (this$disabledAt == null ? other$disabledAt != null : !this$disabledAt.equals(other$disabledAt)) return false;
		return true;
	}

	protected boolean canEqual(final Object other) {
		return other instanceof ArchivedProjectCollaborationDTO;
	}

	@Override
	public int hashCode() {
		final int PRIME = 59;
		int result = 1;
		final Object $contributorId = this.getContributorId();
		result = result * PRIME + ($contributorId == null ? 43 : $contributorId.hashCode());
		final Object $projectId = this.getProjectId();
		result = result * PRIME + ($projectId == null ? 43 : $projectId.hashCode());
		final Object $projectName = this.getProjectName();
		result = result * PRIME + ($projectName == null ? 43 : $projectName.hashCode());
		final Object $ownerId = this.getOwnerId();
		result = result * PRIME + ($ownerId == null ? 43 : $ownerId.hashCode());
		final Object $generator = this.getGenerator();
		result = result * PRIME + ($generator == null ? 43 : $generator.hashCode());
		final Object $inviteToken = this.getInviteToken();
		result = result * PRIME + ($inviteToken == null ? 43 : $inviteToken.hashCode());
		final Object $disabledAt = this.getDisabledAt();
		result = result * PRIME + ($disabledAt == null ? 43 : $disabledAt.hashCode());
		return result;
	}

	@Override
	public String toString() {
		return "ArchivedProjectCollaborationDTO(contributorId=" + this.getContributorId() + ", projectId=" + this.getProjectId() + ", projectName=" + this.getProjectName() + ", ownerId=" + this.getOwnerId() + ", generator=" + this.getGenerator() + ", inviteToken=" + this.getInviteToken() + ", disabledAt=" + this.getDisabledAt() + ")";
	}

	public ArchivedProjectCollaborationDTO() {
	}

	public ArchivedProjectCollaborationDTO(final UUID contributorId, final UUID projectId, final String projectName, final String ownerId, final String generator, final String inviteToken, final OffsetDateTime disabledAt) {
		this.contributorId = contributorId;
		this.projectId = projectId;
		this.projectName = projectName;
		this.ownerId = ownerId;
		this.generator = generator;
		this.inviteToken = inviteToken;
		this.disabledAt = disabledAt;
	}
}
