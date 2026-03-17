package com.src.main.auth.model;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.src.main.auth.config.RbacDbTables;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = RbacDbTables.ROUTES)
public class AuthRoute {
	@Id
	private UUID id;

	@Column(name = "path_pattern", nullable = false, length = 255)
	private String pathPattern;

	@Column(name = "http_method", length = 10)
	private String httpMethod;

	@Column(name = "authority_name", length = 100)
	private String authorityName;

	@Column(name = "priority", nullable = false)
	private int priority;

	@Column(name = "active", nullable = false)
	private boolean active = true;

	@Column(name = "created_at", nullable = false, updatable = false)
	private OffsetDateTime createdAt;

	@Column(name = "updated_at", nullable = false)
	private OffsetDateTime updatedAt;

	@PrePersist
	public void prePersist() {
		if (id == null) {
			id = UUID.randomUUID();
		}
		OffsetDateTime now = OffsetDateTime.now();
		if (createdAt == null) {
			createdAt = now;
		}
		if (updatedAt == null) {
			updatedAt = now;
		}
	}

	@PreUpdate
	public void preUpdate() {
		updatedAt = OffsetDateTime.now();
	}

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public String getPathPattern() {
		return pathPattern;
	}

	public void setPathPattern(String pathPattern) {
		this.pathPattern = pathPattern;
	}

	public String getHttpMethod() {
		return httpMethod;
	}

	public void setHttpMethod(String httpMethod) {
		this.httpMethod = httpMethod;
	}

	public String getAuthorityName() {
		return authorityName;
	}

	public void setAuthorityName(String authorityName) {
		this.authorityName = authorityName;
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public OffsetDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(OffsetDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public OffsetDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(OffsetDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}
}
