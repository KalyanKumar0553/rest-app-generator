package com.src.main.dto;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ProjectDetailsDTO {
	private String projectId;
	private UUID id;
	private String name;
	private String description;
	private String generator;
	private String artifact;
	private String yaml;
	private Map<String, Object> draftData;
	private Integer draftVersion;
	private List<ProjectTabDefinitionDTO> tabDetails;
	private String ownerId;
	private boolean contributorAccess;
	private boolean canManageContributors;
	private String collaborationInviteToken;
	private List<ProjectContributorDTO> contributors;
	private List<ProjectCollaborationRequestDTO> collaborationRequests;
	private UUID latestRunId;
	private String latestRunStatus;
	private Integer latestRunNumber;
	private boolean latestRunHasZip;
	private String latestRunZipBase64;
	private String latestRunZipFileName;
	private OffsetDateTime createdAt;
	private OffsetDateTime updatedAt;

	public String getProjectId() {
		return this.projectId;
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

	public String getArtifact() {
		return this.artifact;
	}

	public String getYaml() {
		return this.yaml;
	}

	public Map<String, Object> getDraftData() {
		return this.draftData;
	}

	public Integer getDraftVersion() {
		return this.draftVersion;
	}

	public List<ProjectTabDefinitionDTO> getTabDetails() {
		return this.tabDetails;
	}

	public String getOwnerId() {
		return this.ownerId;
	}

	public boolean isContributorAccess() {
		return this.contributorAccess;
	}

	public boolean isCanManageContributors() {
		return this.canManageContributors;
	}

	public String getCollaborationInviteToken() {
		return this.collaborationInviteToken;
	}

	public List<ProjectContributorDTO> getContributors() {
		return this.contributors;
	}

	public List<ProjectCollaborationRequestDTO> getCollaborationRequests() {
		return this.collaborationRequests;
	}

	public UUID getLatestRunId() {
		return this.latestRunId;
	}

	public String getLatestRunStatus() {
		return this.latestRunStatus;
	}

	public Integer getLatestRunNumber() {
		return this.latestRunNumber;
	}

	public boolean isLatestRunHasZip() {
		return this.latestRunHasZip;
	}

	public String getLatestRunZipBase64() {
		return this.latestRunZipBase64;
	}

	public String getLatestRunZipFileName() {
		return this.latestRunZipFileName;
	}

	public OffsetDateTime getCreatedAt() {
		return this.createdAt;
	}

	public OffsetDateTime getUpdatedAt() {
		return this.updatedAt;
	}

	public void setProjectId(final String projectId) {
		this.projectId = projectId;
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

	public void setArtifact(final String artifact) {
		this.artifact = artifact;
	}

	public void setYaml(final String yaml) {
		this.yaml = yaml;
	}

	public void setDraftData(final Map<String, Object> draftData) {
		this.draftData = draftData;
	}

	public void setDraftVersion(final Integer draftVersion) {
		this.draftVersion = draftVersion;
	}

	public void setTabDetails(final List<ProjectTabDefinitionDTO> tabDetails) {
		this.tabDetails = tabDetails;
	}

	public void setOwnerId(final String ownerId) {
		this.ownerId = ownerId;
	}

	public void setContributorAccess(final boolean contributorAccess) {
		this.contributorAccess = contributorAccess;
	}

	public void setCanManageContributors(final boolean canManageContributors) {
		this.canManageContributors = canManageContributors;
	}

	public void setCollaborationInviteToken(final String collaborationInviteToken) {
		this.collaborationInviteToken = collaborationInviteToken;
	}

	public void setContributors(final List<ProjectContributorDTO> contributors) {
		this.contributors = contributors;
	}

	public void setCollaborationRequests(final List<ProjectCollaborationRequestDTO> collaborationRequests) {
		this.collaborationRequests = collaborationRequests;
	}

	public void setLatestRunId(final UUID latestRunId) {
		this.latestRunId = latestRunId;
	}

	public void setLatestRunStatus(final String latestRunStatus) {
		this.latestRunStatus = latestRunStatus;
	}

	public void setLatestRunNumber(final Integer latestRunNumber) {
		this.latestRunNumber = latestRunNumber;
	}

	public void setLatestRunHasZip(final boolean latestRunHasZip) {
		this.latestRunHasZip = latestRunHasZip;
	}

	public void setLatestRunZipBase64(final String latestRunZipBase64) {
		this.latestRunZipBase64 = latestRunZipBase64;
	}

	public void setLatestRunZipFileName(final String latestRunZipFileName) {
		this.latestRunZipFileName = latestRunZipFileName;
	}

	public void setCreatedAt(final OffsetDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public void setUpdatedAt(final OffsetDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}

	@Override
	public boolean equals(final Object o) {
		if (o == this) return true;
		if (!(o instanceof ProjectDetailsDTO)) return false;
		final ProjectDetailsDTO other = (ProjectDetailsDTO) o;
		if (!other.canEqual((Object) this)) return false;
		if (this.isContributorAccess() != other.isContributorAccess()) return false;
		if (this.isCanManageContributors() != other.isCanManageContributors()) return false;
		if (this.isLatestRunHasZip() != other.isLatestRunHasZip()) return false;
		final Object this$draftVersion = this.getDraftVersion();
		final Object other$draftVersion = other.getDraftVersion();
		if (this$draftVersion == null ? other$draftVersion != null : !this$draftVersion.equals(other$draftVersion)) return false;
		final Object this$latestRunNumber = this.getLatestRunNumber();
		final Object other$latestRunNumber = other.getLatestRunNumber();
		if (this$latestRunNumber == null ? other$latestRunNumber != null : !this$latestRunNumber.equals(other$latestRunNumber)) return false;
		final Object this$projectId = this.getProjectId();
		final Object other$projectId = other.getProjectId();
		if (this$projectId == null ? other$projectId != null : !this$projectId.equals(other$projectId)) return false;
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
		final Object this$artifact = this.getArtifact();
		final Object other$artifact = other.getArtifact();
		if (this$artifact == null ? other$artifact != null : !this$artifact.equals(other$artifact)) return false;
		final Object this$yaml = this.getYaml();
		final Object other$yaml = other.getYaml();
		if (this$yaml == null ? other$yaml != null : !this$yaml.equals(other$yaml)) return false;
		final Object this$draftData = this.getDraftData();
		final Object other$draftData = other.getDraftData();
		if (this$draftData == null ? other$draftData != null : !this$draftData.equals(other$draftData)) return false;
		final Object this$tabDetails = this.getTabDetails();
		final Object other$tabDetails = other.getTabDetails();
		if (this$tabDetails == null ? other$tabDetails != null : !this$tabDetails.equals(other$tabDetails)) return false;
		final Object this$ownerId = this.getOwnerId();
		final Object other$ownerId = other.getOwnerId();
		if (this$ownerId == null ? other$ownerId != null : !this$ownerId.equals(other$ownerId)) return false;
		final Object this$collaborationInviteToken = this.getCollaborationInviteToken();
		final Object other$collaborationInviteToken = other.getCollaborationInviteToken();
		if (this$collaborationInviteToken == null ? other$collaborationInviteToken != null : !this$collaborationInviteToken.equals(other$collaborationInviteToken)) return false;
		final Object this$contributors = this.getContributors();
		final Object other$contributors = other.getContributors();
		if (this$contributors == null ? other$contributors != null : !this$contributors.equals(other$contributors)) return false;
		final Object this$collaborationRequests = this.getCollaborationRequests();
		final Object other$collaborationRequests = other.getCollaborationRequests();
		if (this$collaborationRequests == null ? other$collaborationRequests != null : !this$collaborationRequests.equals(other$collaborationRequests)) return false;
		final Object this$latestRunId = this.getLatestRunId();
		final Object other$latestRunId = other.getLatestRunId();
		if (this$latestRunId == null ? other$latestRunId != null : !this$latestRunId.equals(other$latestRunId)) return false;
		final Object this$latestRunStatus = this.getLatestRunStatus();
		final Object other$latestRunStatus = other.getLatestRunStatus();
		if (this$latestRunStatus == null ? other$latestRunStatus != null : !this$latestRunStatus.equals(other$latestRunStatus)) return false;
		final Object this$latestRunZipBase64 = this.getLatestRunZipBase64();
		final Object other$latestRunZipBase64 = other.getLatestRunZipBase64();
		if (this$latestRunZipBase64 == null ? other$latestRunZipBase64 != null : !this$latestRunZipBase64.equals(other$latestRunZipBase64)) return false;
		final Object this$latestRunZipFileName = this.getLatestRunZipFileName();
		final Object other$latestRunZipFileName = other.getLatestRunZipFileName();
		if (this$latestRunZipFileName == null ? other$latestRunZipFileName != null : !this$latestRunZipFileName.equals(other$latestRunZipFileName)) return false;
		final Object this$createdAt = this.getCreatedAt();
		final Object other$createdAt = other.getCreatedAt();
		if (this$createdAt == null ? other$createdAt != null : !this$createdAt.equals(other$createdAt)) return false;
		final Object this$updatedAt = this.getUpdatedAt();
		final Object other$updatedAt = other.getUpdatedAt();
		if (this$updatedAt == null ? other$updatedAt != null : !this$updatedAt.equals(other$updatedAt)) return false;
		return true;
	}

	protected boolean canEqual(final Object other) {
		return other instanceof ProjectDetailsDTO;
	}

	@Override
	public int hashCode() {
		final int PRIME = 59;
		int result = 1;
		result = result * PRIME + (this.isContributorAccess() ? 79 : 97);
		result = result * PRIME + (this.isCanManageContributors() ? 79 : 97);
		result = result * PRIME + (this.isLatestRunHasZip() ? 79 : 97);
		final Object $draftVersion = this.getDraftVersion();
		result = result * PRIME + ($draftVersion == null ? 43 : $draftVersion.hashCode());
		final Object $latestRunNumber = this.getLatestRunNumber();
		result = result * PRIME + ($latestRunNumber == null ? 43 : $latestRunNumber.hashCode());
		final Object $projectId = this.getProjectId();
		result = result * PRIME + ($projectId == null ? 43 : $projectId.hashCode());
		final Object $id = this.getId();
		result = result * PRIME + ($id == null ? 43 : $id.hashCode());
		final Object $name = this.getName();
		result = result * PRIME + ($name == null ? 43 : $name.hashCode());
		final Object $description = this.getDescription();
		result = result * PRIME + ($description == null ? 43 : $description.hashCode());
		final Object $generator = this.getGenerator();
		result = result * PRIME + ($generator == null ? 43 : $generator.hashCode());
		final Object $artifact = this.getArtifact();
		result = result * PRIME + ($artifact == null ? 43 : $artifact.hashCode());
		final Object $yaml = this.getYaml();
		result = result * PRIME + ($yaml == null ? 43 : $yaml.hashCode());
		final Object $draftData = this.getDraftData();
		result = result * PRIME + ($draftData == null ? 43 : $draftData.hashCode());
		final Object $tabDetails = this.getTabDetails();
		result = result * PRIME + ($tabDetails == null ? 43 : $tabDetails.hashCode());
		final Object $ownerId = this.getOwnerId();
		result = result * PRIME + ($ownerId == null ? 43 : $ownerId.hashCode());
		final Object $collaborationInviteToken = this.getCollaborationInviteToken();
		result = result * PRIME + ($collaborationInviteToken == null ? 43 : $collaborationInviteToken.hashCode());
		final Object $contributors = this.getContributors();
		result = result * PRIME + ($contributors == null ? 43 : $contributors.hashCode());
		final Object $collaborationRequests = this.getCollaborationRequests();
		result = result * PRIME + ($collaborationRequests == null ? 43 : $collaborationRequests.hashCode());
		final Object $latestRunId = this.getLatestRunId();
		result = result * PRIME + ($latestRunId == null ? 43 : $latestRunId.hashCode());
		final Object $latestRunStatus = this.getLatestRunStatus();
		result = result * PRIME + ($latestRunStatus == null ? 43 : $latestRunStatus.hashCode());
		final Object $latestRunZipBase64 = this.getLatestRunZipBase64();
		result = result * PRIME + ($latestRunZipBase64 == null ? 43 : $latestRunZipBase64.hashCode());
		final Object $latestRunZipFileName = this.getLatestRunZipFileName();
		result = result * PRIME + ($latestRunZipFileName == null ? 43 : $latestRunZipFileName.hashCode());
		final Object $createdAt = this.getCreatedAt();
		result = result * PRIME + ($createdAt == null ? 43 : $createdAt.hashCode());
		final Object $updatedAt = this.getUpdatedAt();
		result = result * PRIME + ($updatedAt == null ? 43 : $updatedAt.hashCode());
		return result;
	}

	@Override
	public String toString() {
		return "ProjectDetailsDTO(projectId=" + this.getProjectId() + ", id=" + this.getId() + ", name=" + this.getName() + ", description=" + this.getDescription() + ", generator=" + this.getGenerator() + ", artifact=" + this.getArtifact() + ", yaml=" + this.getYaml() + ", draftData=" + this.getDraftData() + ", draftVersion=" + this.getDraftVersion() + ", tabDetails=" + this.getTabDetails() + ", ownerId=" + this.getOwnerId() + ", contributorAccess=" + this.isContributorAccess() + ", canManageContributors=" + this.isCanManageContributors() + ", collaborationInviteToken=" + this.getCollaborationInviteToken() + ", contributors=" + this.getContributors() + ", collaborationRequests=" + this.getCollaborationRequests() + ", latestRunId=" + this.getLatestRunId() + ", latestRunStatus=" + this.getLatestRunStatus() + ", latestRunNumber=" + this.getLatestRunNumber() + ", latestRunHasZip=" + this.isLatestRunHasZip() + ", latestRunZipBase64=" + this.getLatestRunZipBase64() + ", latestRunZipFileName=" + this.getLatestRunZipFileName() + ", createdAt=" + this.getCreatedAt() + ", updatedAt=" + this.getUpdatedAt() + ")";
	}

	public ProjectDetailsDTO() {
	}

	public ProjectDetailsDTO(final String projectId, final UUID id, final String name, final String description, final String generator, final String artifact, final String yaml, final Map<String, Object> draftData, final Integer draftVersion, final List<ProjectTabDefinitionDTO> tabDetails, final String ownerId, final boolean contributorAccess, final boolean canManageContributors, final String collaborationInviteToken, final List<ProjectContributorDTO> contributors, final List<ProjectCollaborationRequestDTO> collaborationRequests, final UUID latestRunId, final String latestRunStatus, final Integer latestRunNumber, final boolean latestRunHasZip, final String latestRunZipBase64, final String latestRunZipFileName, final OffsetDateTime createdAt, final OffsetDateTime updatedAt) {
		this.projectId = projectId;
		this.id = id;
		this.name = name;
		this.description = description;
		this.generator = generator;
		this.artifact = artifact;
		this.yaml = yaml;
		this.draftData = draftData;
		this.draftVersion = draftVersion;
		this.tabDetails = tabDetails;
		this.ownerId = ownerId;
		this.contributorAccess = contributorAccess;
		this.canManageContributors = canManageContributors;
		this.collaborationInviteToken = collaborationInviteToken;
		this.contributors = contributors;
		this.collaborationRequests = collaborationRequests;
		this.latestRunId = latestRunId;
		this.latestRunStatus = latestRunStatus;
		this.latestRunNumber = latestRunNumber;
		this.latestRunHasZip = latestRunHasZip;
		this.latestRunZipBase64 = latestRunZipBase64;
		this.latestRunZipFileName = latestRunZipFileName;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}
}
