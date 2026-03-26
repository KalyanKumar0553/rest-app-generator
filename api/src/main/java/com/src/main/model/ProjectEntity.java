package com.src.main.model;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import com.src.main.config.AppDbTables;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = AppDbTables.PROJECTS)
public class ProjectEntity {
	@Id
	private UUID id;
	@JdbcTypeCode(SqlTypes.LONGVARCHAR)
	@Column(nullable = false, columnDefinition = "text")
	private String yaml;
	@JdbcTypeCode(SqlTypes.LONGVARCHAR)
	@Column(name = "draft_data", columnDefinition = "text")
	private String draftData;
	@Column(name = "draft_version")
	private Integer draftVersion;
	@Column(name = "invite_token", length = 64)
	private String inviteToken;
	@Column
	private String artifact;
	@Column(name = "group_id")
	private String groupId;
	@Column(name = "build_tool")
	private String buildTool;
	@Column
	private String version;
	@Column
	private String packaging;
	@Column(name = "owner_id", nullable = false, length = 100)
	private String ownerId;
	@Column
	private String generator;
	@Column
	private String name;
	@Column
	private String description;
	@Column(name = "spring_boot_version")
	private String springBootVersion;
	@Column(name = "jdk_version")
	private String jdkVersion;
	@Column(name = "include_openapi")
	private boolean includeOpenapi;
	@Column(name = "angular_integration")
	private boolean angularIntegration;
	@Column(name = "created_at", nullable = false)
	private OffsetDateTime createdAt;
	@Column(name = "updated_at", nullable = false)
	private OffsetDateTime updatedAt;
	@Column(name = "error_message")
	private String errorMessage;
	@OneToMany(mappedBy = "project")
	private List<ProjectContributorEntity> contributors = new ArrayList<>();
	@OneToMany(mappedBy = "project")
	private List<ProjectCollaborationRequestEntity> collaborationRequests = new ArrayList<>();

	public ProjectEntity() {
	}

	public UUID getId() {
		return this.id;
	}

	public String getYaml() {
		return this.yaml;
	}

	public String getDraftData() {
		return this.draftData;
	}

	public Integer getDraftVersion() {
		return this.draftVersion;
	}

	public String getInviteToken() {
		return this.inviteToken;
	}

	public String getArtifact() {
		return this.artifact;
	}

	public String getGroupId() {
		return this.groupId;
	}

	public String getBuildTool() {
		return this.buildTool;
	}

	public String getVersion() {
		return this.version;
	}

	public String getPackaging() {
		return this.packaging;
	}

	public String getOwnerId() {
		return this.ownerId;
	}

	public String getGenerator() {
		return this.generator;
	}

	public String getName() {
		return this.name;
	}

	public String getDescription() {
		return this.description;
	}

	public String getSpringBootVersion() {
		return this.springBootVersion;
	}

	public String getJdkVersion() {
		return this.jdkVersion;
	}

	public boolean isIncludeOpenapi() {
		return this.includeOpenapi;
	}

	public boolean isAngularIntegration() {
		return this.angularIntegration;
	}

	public OffsetDateTime getCreatedAt() {
		return this.createdAt;
	}

	public OffsetDateTime getUpdatedAt() {
		return this.updatedAt;
	}

	public String getErrorMessage() {
		return this.errorMessage;
	}

	public List<ProjectContributorEntity> getContributors() {
		return this.contributors;
	}

	public List<ProjectCollaborationRequestEntity> getCollaborationRequests() {
		return this.collaborationRequests;
	}

	public void setId(final UUID id) {
		this.id = id;
	}

	public void setYaml(final String yaml) {
		this.yaml = yaml;
	}

	public void setDraftData(final String draftData) {
		this.draftData = draftData;
	}

	public void setDraftVersion(final Integer draftVersion) {
		this.draftVersion = draftVersion;
	}

	public void setInviteToken(final String inviteToken) {
		this.inviteToken = inviteToken;
	}

	public void setArtifact(final String artifact) {
		this.artifact = artifact;
	}

	public void setGroupId(final String groupId) {
		this.groupId = groupId;
	}

	public void setBuildTool(final String buildTool) {
		this.buildTool = buildTool;
	}

	public void setVersion(final String version) {
		this.version = version;
	}

	public void setPackaging(final String packaging) {
		this.packaging = packaging;
	}

	public void setOwnerId(final String ownerId) {
		this.ownerId = ownerId;
	}

	public void setGenerator(final String generator) {
		this.generator = generator;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public void setDescription(final String description) {
		this.description = description;
	}

	public void setSpringBootVersion(final String springBootVersion) {
		this.springBootVersion = springBootVersion;
	}

	public void setJdkVersion(final String jdkVersion) {
		this.jdkVersion = jdkVersion;
	}

	public void setIncludeOpenapi(final boolean includeOpenapi) {
		this.includeOpenapi = includeOpenapi;
	}

	public void setAngularIntegration(final boolean angularIntegration) {
		this.angularIntegration = angularIntegration;
	}

	public void setCreatedAt(final OffsetDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public void setUpdatedAt(final OffsetDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}

	public void setErrorMessage(final String errorMessage) {
		this.errorMessage = errorMessage;
	}

	public void setContributors(final List<ProjectContributorEntity> contributors) {
		this.contributors = contributors;
	}

	public void setCollaborationRequests(final List<ProjectCollaborationRequestEntity> collaborationRequests) {
		this.collaborationRequests = collaborationRequests;
	}

	@Override
	public boolean equals(final Object o) {
		if (o == this) return true;
		if (!(o instanceof ProjectEntity)) return false;
		final ProjectEntity other = (ProjectEntity) o;
		if (!other.canEqual((Object) this)) return false;
		if (this.isIncludeOpenapi() != other.isIncludeOpenapi()) return false;
		if (this.isAngularIntegration() != other.isAngularIntegration()) return false;
		final Object this$draftVersion = this.getDraftVersion();
		final Object other$draftVersion = other.getDraftVersion();
		if (this$draftVersion == null ? other$draftVersion != null : !this$draftVersion.equals(other$draftVersion)) return false;
		final Object this$id = this.getId();
		final Object other$id = other.getId();
		if (this$id == null ? other$id != null : !this$id.equals(other$id)) return false;
		final Object this$yaml = this.getYaml();
		final Object other$yaml = other.getYaml();
		if (this$yaml == null ? other$yaml != null : !this$yaml.equals(other$yaml)) return false;
		final Object this$draftData = this.getDraftData();
		final Object other$draftData = other.getDraftData();
		if (this$draftData == null ? other$draftData != null : !this$draftData.equals(other$draftData)) return false;
		final Object this$inviteToken = this.getInviteToken();
		final Object other$inviteToken = other.getInviteToken();
		if (this$inviteToken == null ? other$inviteToken != null : !this$inviteToken.equals(other$inviteToken)) return false;
		final Object this$artifact = this.getArtifact();
		final Object other$artifact = other.getArtifact();
		if (this$artifact == null ? other$artifact != null : !this$artifact.equals(other$artifact)) return false;
		final Object this$groupId = this.getGroupId();
		final Object other$groupId = other.getGroupId();
		if (this$groupId == null ? other$groupId != null : !this$groupId.equals(other$groupId)) return false;
		final Object this$buildTool = this.getBuildTool();
		final Object other$buildTool = other.getBuildTool();
		if (this$buildTool == null ? other$buildTool != null : !this$buildTool.equals(other$buildTool)) return false;
		final Object this$version = this.getVersion();
		final Object other$version = other.getVersion();
		if (this$version == null ? other$version != null : !this$version.equals(other$version)) return false;
		final Object this$packaging = this.getPackaging();
		final Object other$packaging = other.getPackaging();
		if (this$packaging == null ? other$packaging != null : !this$packaging.equals(other$packaging)) return false;
		final Object this$ownerId = this.getOwnerId();
		final Object other$ownerId = other.getOwnerId();
		if (this$ownerId == null ? other$ownerId != null : !this$ownerId.equals(other$ownerId)) return false;
		final Object this$generator = this.getGenerator();
		final Object other$generator = other.getGenerator();
		if (this$generator == null ? other$generator != null : !this$generator.equals(other$generator)) return false;
		final Object this$name = this.getName();
		final Object other$name = other.getName();
		if (this$name == null ? other$name != null : !this$name.equals(other$name)) return false;
		final Object this$description = this.getDescription();
		final Object other$description = other.getDescription();
		if (this$description == null ? other$description != null : !this$description.equals(other$description)) return false;
		final Object this$springBootVersion = this.getSpringBootVersion();
		final Object other$springBootVersion = other.getSpringBootVersion();
		if (this$springBootVersion == null ? other$springBootVersion != null : !this$springBootVersion.equals(other$springBootVersion)) return false;
		final Object this$jdkVersion = this.getJdkVersion();
		final Object other$jdkVersion = other.getJdkVersion();
		if (this$jdkVersion == null ? other$jdkVersion != null : !this$jdkVersion.equals(other$jdkVersion)) return false;
		final Object this$createdAt = this.getCreatedAt();
		final Object other$createdAt = other.getCreatedAt();
		if (this$createdAt == null ? other$createdAt != null : !this$createdAt.equals(other$createdAt)) return false;
		final Object this$updatedAt = this.getUpdatedAt();
		final Object other$updatedAt = other.getUpdatedAt();
		if (this$updatedAt == null ? other$updatedAt != null : !this$updatedAt.equals(other$updatedAt)) return false;
		final Object this$errorMessage = this.getErrorMessage();
		final Object other$errorMessage = other.getErrorMessage();
		if (this$errorMessage == null ? other$errorMessage != null : !this$errorMessage.equals(other$errorMessage)) return false;
		return true;
	}

	protected boolean canEqual(final Object other) {
		return other instanceof ProjectEntity;
	}

	@Override
	public int hashCode() {
		final int PRIME = 59;
		int result = 1;
		result = result * PRIME + (this.isIncludeOpenapi() ? 79 : 97);
		result = result * PRIME + (this.isAngularIntegration() ? 79 : 97);
		final Object $draftVersion = this.getDraftVersion();
		result = result * PRIME + ($draftVersion == null ? 43 : $draftVersion.hashCode());
		final Object $id = this.getId();
		result = result * PRIME + ($id == null ? 43 : $id.hashCode());
		final Object $yaml = this.getYaml();
		result = result * PRIME + ($yaml == null ? 43 : $yaml.hashCode());
		final Object $draftData = this.getDraftData();
		result = result * PRIME + ($draftData == null ? 43 : $draftData.hashCode());
		final Object $inviteToken = this.getInviteToken();
		result = result * PRIME + ($inviteToken == null ? 43 : $inviteToken.hashCode());
		final Object $artifact = this.getArtifact();
		result = result * PRIME + ($artifact == null ? 43 : $artifact.hashCode());
		final Object $groupId = this.getGroupId();
		result = result * PRIME + ($groupId == null ? 43 : $groupId.hashCode());
		final Object $buildTool = this.getBuildTool();
		result = result * PRIME + ($buildTool == null ? 43 : $buildTool.hashCode());
		final Object $version = this.getVersion();
		result = result * PRIME + ($version == null ? 43 : $version.hashCode());
		final Object $packaging = this.getPackaging();
		result = result * PRIME + ($packaging == null ? 43 : $packaging.hashCode());
		final Object $ownerId = this.getOwnerId();
		result = result * PRIME + ($ownerId == null ? 43 : $ownerId.hashCode());
		final Object $generator = this.getGenerator();
		result = result * PRIME + ($generator == null ? 43 : $generator.hashCode());
		final Object $name = this.getName();
		result = result * PRIME + ($name == null ? 43 : $name.hashCode());
		final Object $description = this.getDescription();
		result = result * PRIME + ($description == null ? 43 : $description.hashCode());
		final Object $springBootVersion = this.getSpringBootVersion();
		result = result * PRIME + ($springBootVersion == null ? 43 : $springBootVersion.hashCode());
		final Object $jdkVersion = this.getJdkVersion();
		result = result * PRIME + ($jdkVersion == null ? 43 : $jdkVersion.hashCode());
		final Object $createdAt = this.getCreatedAt();
		result = result * PRIME + ($createdAt == null ? 43 : $createdAt.hashCode());
		final Object $updatedAt = this.getUpdatedAt();
		result = result * PRIME + ($updatedAt == null ? 43 : $updatedAt.hashCode());
		final Object $errorMessage = this.getErrorMessage();
		result = result * PRIME + ($errorMessage == null ? 43 : $errorMessage.hashCode());
		return result;
	}

	@Override
	public String toString() {
		return "ProjectEntity(id=" + this.getId() + ", yaml=" + this.getYaml() + ", draftData=" + this.getDraftData() + ", draftVersion=" + this.getDraftVersion() + ", inviteToken=" + this.getInviteToken() + ", artifact=" + this.getArtifact() + ", groupId=" + this.getGroupId() + ", buildTool=" + this.getBuildTool() + ", version=" + this.getVersion() + ", packaging=" + this.getPackaging() + ", ownerId=" + this.getOwnerId() + ", generator=" + this.getGenerator() + ", name=" + this.getName() + ", description=" + this.getDescription() + ", springBootVersion=" + this.getSpringBootVersion() + ", jdkVersion=" + this.getJdkVersion() + ", includeOpenapi=" + this.isIncludeOpenapi() + ", angularIntegration=" + this.isAngularIntegration() + ", createdAt=" + this.getCreatedAt() + ", updatedAt=" + this.getUpdatedAt() + ", errorMessage=" + this.getErrorMessage() + ")";
	}
}
