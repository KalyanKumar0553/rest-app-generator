package com.src.main.model;

import jakarta.persistence.*;
import lombok.Data;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.src.main.utils.ProjectStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "projects")
@Data
public class ProjectEntity {
	@Id
	private UUID id;

	@JdbcTypeCode(SqlTypes.LONGVARCHAR)
	@Column(nullable = false, columnDefinition = "text")
	private String yaml;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private ProjectStatus status;
	
	@Column(name = "artifact_id", nullable = false)
	private String artifact;
	
	@Column(name = "group_id", nullable = false)
	private String groupId;
	
	@Column(name = "build_tool", nullable = false)
	private String buildTool;
	
	@Column(nullable = false)
	private String version;
	
	@Column(name = "created_at", nullable = false)
	private OffsetDateTime createdAt;
	
	@Column(name = "updated_at", nullable = false)
	private OffsetDateTime updatedAt;

	@JdbcTypeCode(SqlTypes.LONGVARBINARY)
	@Column(columnDefinition = "bytea")
	private byte[] zip;

	@Column(name = "error_message")
	private String errorMessage;

}
