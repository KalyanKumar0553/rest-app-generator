package com.src.main.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CommunicationConfigRequestDTO {
	@NotBlank
	@Size(max = 50)
	private String serviceType;
	@NotBlank
	@Size(max = 200)
	private String displayName;
	private boolean enabled;
	@Size(max = 500)
	private String endpoint;
	@Size(max = 200)
	private String senderId;
	@Size(max = 200)
	private String channelRegistrationId;
	@Size(max = 4000)
	private String connectionString;

	public String getServiceType() { return serviceType; }
	public String getDisplayName() { return displayName; }
	public boolean isEnabled() { return enabled; }
	public String getEndpoint() { return endpoint; }
	public String getSenderId() { return senderId; }
	public String getChannelRegistrationId() { return channelRegistrationId; }
	public String getConnectionString() { return connectionString; }

	public void setServiceType(String serviceType) { this.serviceType = serviceType; }
	public void setDisplayName(String displayName) { this.displayName = displayName; }
	public void setEnabled(boolean enabled) { this.enabled = enabled; }
	public void setEndpoint(String endpoint) { this.endpoint = endpoint; }
	public void setSenderId(String senderId) { this.senderId = senderId; }
	public void setChannelRegistrationId(String channelRegistrationId) { this.channelRegistrationId = channelRegistrationId; }
	public void setConnectionString(String connectionString) { this.connectionString = connectionString; }
}
