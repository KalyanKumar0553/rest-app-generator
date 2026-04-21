package com.src.main.communication.service.sender;

import java.util.Optional;

import com.src.main.communication.model.CommunicationChannelType;

public interface CommunicationConfigProvider {
	Optional<ChannelConfig> getEnabledConfig(CommunicationChannelType channelType);

	record ChannelConfig(String endpoint, String accessKey, String senderId, String channelRegistrationId, String connectionString) {
	}
}
