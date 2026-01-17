package com.src.main.model;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.src.main.util.ProjectRunStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "projects")
@Data
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

}
