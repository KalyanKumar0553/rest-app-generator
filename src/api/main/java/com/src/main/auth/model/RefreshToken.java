package com.src.main.auth.model;

import java.time.Instant;

import com.src.main.auth.config.AuthDbTables;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = AuthDbTables.REFRESH_TOKENS)
public class RefreshToken {
	@Id
	@Column(name = "id", nullable = false, updatable = false)
	private String id;

	@Column(name = "user_id", nullable = false)
	private String userId;

	@Column(name = "family_id", nullable = false)
	private String familyId;

	@Column(name = "token_hash", nullable = false)
	private String tokenHash;

	@Column(name = "expires_at", nullable = false)
	private Instant expiresAt;

	@Column(name = "revoked", nullable = false)
	private boolean revoked;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@ManyToOne
	@JoinColumn(name = "user_id", referencedColumnName = "id", insertable = false, updatable = false)
	private User user;

	@PrePersist
	public void prePersist() {
		if (id == null) {
			id = java.util.UUID.randomUUID().toString();
		}
		createdAt = Instant.now();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getFamilyId() {
		return familyId;
	}

	public void setFamilyId(String familyId) {
		this.familyId = familyId;
	}

	public String getTokenHash() {
		return tokenHash;
	}

	public void setTokenHash(String tokenHash) {
		this.tokenHash = tokenHash;
	}

	public Instant getExpiresAt() {
		return expiresAt;
	}

	public void setExpiresAt(Instant expiresAt) {
		this.expiresAt = expiresAt;
	}

	public boolean isRevoked() {
		return revoked;
	}

	public void setRevoked(boolean revoked) {
		this.revoked = revoked;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}
}
