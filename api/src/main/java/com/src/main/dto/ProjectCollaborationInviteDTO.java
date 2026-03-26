package com.src.main.dto;

public class ProjectCollaborationInviteDTO {
	private String inviteToken;
	private String projectId;
	private String projectName;
	private String generator;
	private String ownerId;
	private boolean contributorAccess;
	private boolean requestPending;

	public String getInviteToken() {
		return this.inviteToken;
	}

	public String getProjectId() {
		return this.projectId;
	}

	public String getProjectName() {
		return this.projectName;
	}

	public String getGenerator() {
		return this.generator;
	}

	public String getOwnerId() {
		return this.ownerId;
	}

	public boolean isContributorAccess() {
		return this.contributorAccess;
	}

	public boolean isRequestPending() {
		return this.requestPending;
	}

	public void setInviteToken(final String inviteToken) {
		this.inviteToken = inviteToken;
	}

	public void setProjectId(final String projectId) {
		this.projectId = projectId;
	}

	public void setProjectName(final String projectName) {
		this.projectName = projectName;
	}

	public void setGenerator(final String generator) {
		this.generator = generator;
	}

	public void setOwnerId(final String ownerId) {
		this.ownerId = ownerId;
	}

	public void setContributorAccess(final boolean contributorAccess) {
		this.contributorAccess = contributorAccess;
	}

	public void setRequestPending(final boolean requestPending) {
		this.requestPending = requestPending;
	}

	@Override
	public boolean equals(final Object o) {
		if (o == this) return true;
		if (!(o instanceof ProjectCollaborationInviteDTO)) return false;
		final ProjectCollaborationInviteDTO other = (ProjectCollaborationInviteDTO) o;
		if (!other.canEqual((Object) this)) return false;
		if (this.isContributorAccess() != other.isContributorAccess()) return false;
		if (this.isRequestPending() != other.isRequestPending()) return false;
		final Object this$inviteToken = this.getInviteToken();
		final Object other$inviteToken = other.getInviteToken();
		if (this$inviteToken == null ? other$inviteToken != null : !this$inviteToken.equals(other$inviteToken)) return false;
		final Object this$projectId = this.getProjectId();
		final Object other$projectId = other.getProjectId();
		if (this$projectId == null ? other$projectId != null : !this$projectId.equals(other$projectId)) return false;
		final Object this$projectName = this.getProjectName();
		final Object other$projectName = other.getProjectName();
		if (this$projectName == null ? other$projectName != null : !this$projectName.equals(other$projectName)) return false;
		final Object this$generator = this.getGenerator();
		final Object other$generator = other.getGenerator();
		if (this$generator == null ? other$generator != null : !this$generator.equals(other$generator)) return false;
		final Object this$ownerId = this.getOwnerId();
		final Object other$ownerId = other.getOwnerId();
		if (this$ownerId == null ? other$ownerId != null : !this$ownerId.equals(other$ownerId)) return false;
		return true;
	}

	protected boolean canEqual(final Object other) {
		return other instanceof ProjectCollaborationInviteDTO;
	}

	@Override
	public int hashCode() {
		final int PRIME = 59;
		int result = 1;
		result = result * PRIME + (this.isContributorAccess() ? 79 : 97);
		result = result * PRIME + (this.isRequestPending() ? 79 : 97);
		final Object $inviteToken = this.getInviteToken();
		result = result * PRIME + ($inviteToken == null ? 43 : $inviteToken.hashCode());
		final Object $projectId = this.getProjectId();
		result = result * PRIME + ($projectId == null ? 43 : $projectId.hashCode());
		final Object $projectName = this.getProjectName();
		result = result * PRIME + ($projectName == null ? 43 : $projectName.hashCode());
		final Object $generator = this.getGenerator();
		result = result * PRIME + ($generator == null ? 43 : $generator.hashCode());
		final Object $ownerId = this.getOwnerId();
		result = result * PRIME + ($ownerId == null ? 43 : $ownerId.hashCode());
		return result;
	}

	@Override
	public String toString() {
		return "ProjectCollaborationInviteDTO(inviteToken=" + this.getInviteToken() + ", projectId=" + this.getProjectId() + ", projectName=" + this.getProjectName() + ", generator=" + this.getGenerator() + ", ownerId=" + this.getOwnerId() + ", contributorAccess=" + this.isContributorAccess() + ", requestPending=" + this.isRequestPending() + ")";
	}

	public ProjectCollaborationInviteDTO() {
	}

	public ProjectCollaborationInviteDTO(final String inviteToken, final String projectId, final String projectName, final String generator, final String ownerId, final boolean contributorAccess, final boolean requestPending) {
		this.inviteToken = inviteToken;
		this.projectId = projectId;
		this.projectName = projectName;
		this.generator = generator;
		this.ownerId = ownerId;
		this.contributorAccess = contributorAccess;
		this.requestPending = requestPending;
	}
}
