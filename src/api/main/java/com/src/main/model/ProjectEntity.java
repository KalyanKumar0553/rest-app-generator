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
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Entity
@Table(name = AppDbTables.PROJECTS)
@Data
public class ProjectEntity {
	@Id
	private UUID id;

	@JdbcTypeCode(SqlTypes.LONGVARCHAR)
	@Column(nullable = false, columnDefinition = "text")
	private String yaml;

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
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	private List<ProjectContributorEntity> contributors = new ArrayList<>();
}
