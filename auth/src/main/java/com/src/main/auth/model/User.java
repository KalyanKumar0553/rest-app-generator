package com.src.main.auth.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import com.src.main.auth.config.AuthDbTables;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = AuthDbTables.USERS)
public class User {
	@Id
	@Column(name = "id", nullable = false, updatable = false)
	private String id;

	@Column(name = "identifier", nullable = false, unique = true)
	private String identifier;

	@Column(name = "identifier_hash", length = 64)
	private String identifierHash;

	@Enumerated(EnumType.STRING)
	@JdbcTypeCode(SqlTypes.NAMED_ENUM)
	@Column(name = "identifier_type", nullable = false, columnDefinition = "identifier_type")
	private IdentifierType identifierType;

	@Column(name = "password_hash", nullable = false)
	private String passwordHash;

	@Enumerated(EnumType.STRING)
	@JdbcTypeCode(SqlTypes.NAMED_ENUM)
	@Column(name = "status", nullable = false, columnDefinition = "user_status")
	private UserStatus status = UserStatus.PENDING_VERIFICATION;

	@Column(name = "failed_login_attempts", nullable = false)
	private int failedLoginAttempts;

	@Column(name = "locked_until")
	private Instant lockedUntil;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<OtpRequest> otps = new ArrayList<>();

	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<RefreshToken> refreshTokens = new ArrayList<>();

	@OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
	private UserProfile profile;

	@PrePersist
	public void prePersist() {
		Instant now = Instant.now();
		if (id == null) {
			id = java.util.UUID.randomUUID().toString();
		}
		createdAt = now;
		updatedAt = now;
	}

	@PreUpdate
	public void preUpdate() {
		updatedAt = Instant.now();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	public String getIdentifierHash() {
		return identifierHash;
	}

	public void setIdentifierHash(String identifierHash) {
		this.identifierHash = identifierHash;
	}

	public IdentifierType getIdentifierType() {
		return identifierType;
	}

	public void setIdentifierType(IdentifierType identifierType) {
		this.identifierType = identifierType;
	}

	public String getPasswordHash() {
		return passwordHash;
	}

	public void setPasswordHash(String passwordHash) {
		this.passwordHash = passwordHash;
	}

	public UserStatus getStatus() {
		return status;
	}

	public void setStatus(UserStatus status) {
		this.status = status;
	}

	public int getFailedLoginAttempts() {
		return failedLoginAttempts;
	}

	public void setFailedLoginAttempts(int failedLoginAttempts) {
		this.failedLoginAttempts = failedLoginAttempts;
	}

	public Instant getLockedUntil() {
		return lockedUntil;
	}

	public void setLockedUntil(Instant lockedUntil) {
		this.lockedUntil = lockedUntil;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}
}
