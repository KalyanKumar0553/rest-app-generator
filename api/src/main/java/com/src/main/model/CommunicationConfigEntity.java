package com.src.main.model;

import java.time.Instant;
import java.util.UUID;

import com.src.main.config.AppDbTables;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = AppDbTables.COMMUNICATION_CONFIGS)
public class CommunicationConfigEntity {
	@Id
	@Column(name = "id", nullable = false, updatable = false)
	private UUID id;

	@Column(name = "service_type", nullable = false, length = 50)
	private String serviceType;

	@Column(name = "enabled", nullable = false)
	private boolean enabled;

	@Column(name = "display_name", nullable = false, length = 200)
	private String displayName;

	@Column(name = "endpoint", length = 500)
	private String endpoint;

	@Column(name = "sender_id", length = 200)
	private String senderId;

	@Column(name = "channel_registration_id", length = 200)
	private String channelRegistrationId;

	@Column(name = "connection_string_encrypted", length = 4000)
	private String connectionStringEncrypted;

	@Column(name = "connection_string_hash", length = 128)
	private String connectionStringHash;

	@Column(name = "connection_string_salt", length = 128)
	private String connectionStringSalt;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	@PrePersist
	public void prePersist() {
		if (id == null) {
			id = UUID.randomUUID();
		}
		Instant now = Instant.now();
		createdAt = now;
		updatedAt = now;
	}

	@PreUpdate
	public void preUpdate() {
		updatedAt = Instant.now();
	}

	public UUID getId() { return id; }
	public String getServiceType() { return serviceType; }
	public boolean isEnabled() { return enabled; }
	public String getDisplayName() { return displayName; }
	public String getEndpoint() { return endpoint; }
	public String getSenderId() { return senderId; }
	public String getChannelRegistrationId() { return channelRegistrationId; }
	public String getConnectionStringEncrypted() { return connectionStringEncrypted; }
	public String getConnectionStringHash() { return connectionStringHash; }
	public String getConnectionStringSalt() { return connectionStringSalt; }
	public Instant getCreatedAt() { return createdAt; }
	public Instant getUpdatedAt() { return updatedAt; }

	public void setId(UUID id) { this.id = id; }
	public void setServiceType(String serviceType) { this.serviceType = serviceType; }
	public void setEnabled(boolean enabled) { this.enabled = enabled; }
	public void setDisplayName(String displayName) { this.displayName = displayName; }
	public void setEndpoint(String endpoint) { this.endpoint = endpoint; }
	public void setSenderId(String senderId) { this.senderId = senderId; }
	public void setChannelRegistrationId(String channelRegistrationId) { this.channelRegistrationId = channelRegistrationId; }
	public void setConnectionStringEncrypted(String connectionStringEncrypted) { this.connectionStringEncrypted = connectionStringEncrypted; }
	public void setConnectionStringHash(String connectionStringHash) { this.connectionStringHash = connectionStringHash; }
	public void setConnectionStringSalt(String connectionStringSalt) { this.connectionStringSalt = connectionStringSalt; }
}
