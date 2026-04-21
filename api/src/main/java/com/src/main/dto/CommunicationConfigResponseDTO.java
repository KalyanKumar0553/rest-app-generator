package com.src.main.dto;

import java.time.Instant;
import java.util.UUID;

public class CommunicationConfigResponseDTO {
	private UUID id;
	private String serviceType;
	private boolean enabled;
	private String displayName;
	private String endpoint;
	private String senderId;
	private String channelRegistrationId;
	private Instant createdAt;
	private Instant updatedAt;

	public CommunicationConfigResponseDTO() {}

	public CommunicationConfigResponseDTO(UUID id, String serviceType, boolean enabled, String displayName, String endpoint, String senderId, String channelRegistrationId, Instant createdAt, Instant updatedAt) {
		this.id = id;
		this.serviceType = serviceType;
		this.enabled = enabled;
		this.displayName = displayName;
		this.endpoint = endpoint;
		this.senderId = senderId;
		this.channelRegistrationId = channelRegistrationId;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}

	public UUID getId() { return id; }
	public String getServiceType() { return serviceType; }
	public boolean isEnabled() { return enabled; }
	public String getDisplayName() { return displayName; }
	public String getEndpoint() { return endpoint; }
	public String getSenderId() { return senderId; }
	public String getChannelRegistrationId() { return channelRegistrationId; }
	public Instant getCreatedAt() { return createdAt; }
	public Instant getUpdatedAt() { return updatedAt; }

	public void setId(UUID id) { this.id = id; }
	public void setServiceType(String serviceType) { this.serviceType = serviceType; }
	public void setEnabled(boolean enabled) { this.enabled = enabled; }
	public void setDisplayName(String displayName) { this.displayName = displayName; }
	public void setEndpoint(String endpoint) { this.endpoint = endpoint; }
	public void setSenderId(String senderId) { this.senderId = senderId; }
	public void setChannelRegistrationId(String channelRegistrationId) { this.channelRegistrationId = channelRegistrationId; }
	public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
	public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
