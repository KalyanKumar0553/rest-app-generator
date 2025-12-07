package com.src.main.model;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.src.main.util.ProjectMetaDataConstants;
import com.src.main.util.ProjectStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "projects")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectEntity {
	@Id
	private UUID id;

	@JdbcTypeCode(SqlTypes.LONGVARCHAR)
	@Column(nullable = false, columnDefinition = "text")
	private String yaml;

	@Column
	private String artifact;

	@Column
	private String groupId;

	@Column
	private String buildTool;

	@Column
	private String version;

	@Column
	String packaging;

	@Column(name = "owner_id", nullable = false, length = 100)
	private String ownerId;

	@Column
	String generator;

	@Column
	String name;

	@Column
	String description;

	@Column
	String springBootVersion;

	@Column
	private String jdkVersion;

	@Column
	private boolean includeOpenapi;

	@Column
	private boolean angularIntegration;

	@Column(name = "created_at", nullable = false)
	private OffsetDateTime createdAt;

	@Column(name = "updated_at", nullable = false)
	private OffsetDateTime updatedAt;

	@Column(name = "error_message")
	private String errorMessage;

	@Column(name = "status")
	private String status;

	public static ProjectEntity fromDTO(Map<String, String> dto,String ownerId,String yamlText) {
		return ProjectEntity.builder().id(UUID.randomUUID()).artifact(dto.get(ProjectMetaDataConstants.ARTIFACT_ID))
				.groupId(dto.get(ProjectMetaDataConstants.GROUP_ID)).status(ProjectStatus.ACTIVE.getDescription())
				.version(dto.get(ProjectMetaDataConstants.VERSION))
				.buildTool(dto.get(ProjectMetaDataConstants.BUILD_TOOL))
				.packaging(dto.get(ProjectMetaDataConstants.PACKAGING))
				.generator(dto.get(ProjectMetaDataConstants.GENERATOR))
				.name(dto.get(ProjectMetaDataConstants.NAME))
				.description(dto.get(ProjectMetaDataConstants.DESCRIPTION))
				.springBootVersion(dto.get(ProjectMetaDataConstants.SPRING_BOOT_VERSION))
				.jdkVersion(dto.get(ProjectMetaDataConstants.JDK_VERSION))
				.ownerId(ownerId)
				.yaml(yamlText)
				.createdAt(OffsetDateTime.now())
				.updatedAt(OffsetDateTime.now())
				.errorMessage("")
				.status(ProjectStatus.ACTIVE.getDescription())
				.build();
	}

}
