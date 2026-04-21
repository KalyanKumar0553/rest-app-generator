package com.src.main.communication.service.sender;

import java.util.List;

import org.springframework.stereotype.Component;

import com.azure.communication.messages.NotificationMessagesClient;
import com.azure.communication.messages.NotificationMessagesClientBuilder;
import com.azure.communication.messages.models.SendMessageResult;
import com.azure.communication.messages.models.TextNotificationContent;
import com.src.main.communication.model.CommunicationChannelType;
import com.src.main.communication.model.WhatsAppMessageRequest;

@Component
public class WhatsAppMessageSender implements CommunicationMessageSender<WhatsAppMessageRequest> {

	private final CommunicationConfigProvider configProvider;

	public WhatsAppMessageSender(CommunicationConfigProvider configProvider) {
		this.configProvider = configProvider;
	}

	@Override
	public CommunicationChannelType channelType() {
		return CommunicationChannelType.WHATSAPP;
	}

	@Override
	public void send(WhatsAppMessageRequest request) {
		var config = configProvider.getEnabledConfig(channelType())
				.orElseThrow(() -> new IllegalStateException("WhatsApp communication config is not enabled."));
		NotificationMessagesClient client = new NotificationMessagesClientBuilder()
				.connectionString(require(config.connectionString(), "whatsapp connection string"))
				.buildClient();
		SendMessageResult result = client.send(new TextNotificationContent(
				require(config.channelRegistrationId(), "channel registration id"),
				List.of(request.recipient()),
				request.message()));
		if (result == null) {
			throw new IllegalStateException("WhatsApp message delivery failed.");
		}
	}

	private String require(String value, String fieldName) {
		if (value == null || value.isBlank()) {
			throw new IllegalStateException(fieldName + " is required.");
		}
		return value.trim();
	}
}
