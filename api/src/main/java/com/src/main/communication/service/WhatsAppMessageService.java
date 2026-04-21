package com.src.main.communication.service;

import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;

import com.azure.communication.messages.NotificationMessagesClient;
import com.azure.communication.messages.NotificationMessagesClientBuilder;
import com.azure.communication.messages.models.SendMessageResult;
import com.azure.communication.messages.models.TextNotificationContent;
import com.src.main.exception.GenericException;
import com.src.main.model.CommunicationConfigEntity;
import com.src.main.service.CommunicationConfigService;
import com.src.main.service.DataEncryptionService;

@Service
public class WhatsAppMessageService {
	public static final String SERVICE_TYPE = "WHATSAPP";

	private final CommunicationConfigService communicationConfigService;
	private final DataEncryptionService dataEncryptionService;

	public WhatsAppMessageService(CommunicationConfigService communicationConfigService, DataEncryptionService dataEncryptionService) {
		this.communicationConfigService = communicationConfigService;
		this.dataEncryptionService = dataEncryptionService;
	}

	public SendMessageResult sendMessage(String recipientPhoneNumber, String message) {
		CommunicationConfigEntity config = communicationConfigService.findEnabledConfig(SERVICE_TYPE)
				.orElseThrow(() -> new GenericException(org.springframework.http.HttpStatus.BAD_REQUEST, "WhatsApp communication config is not enabled."));
		String connectionString = decryptConnectionString(config);
		NotificationMessagesClient client = new NotificationMessagesClientBuilder()
				.connectionString(connectionString)
				.buildClient();
		List<String> recipients = Collections.singletonList(recipientPhoneNumber);
		return client.send(new TextNotificationContent(requireText(config.getChannelRegistrationId(), "channelRegistrationId"), recipients, message));
	}

	private String decryptConnectionString(CommunicationConfigEntity config) {
		String encrypted = config.getConnectionStringEncrypted();
		if (encrypted == null || encrypted.isBlank()) {
			throw new GenericException(org.springframework.http.HttpStatus.BAD_REQUEST, "WhatsApp connection string is missing.");
		}
		return dataEncryptionService.decrypt(encrypted);
	}

	private String requireText(String value, String fieldName) {
		if (value == null || value.isBlank()) {
			throw new GenericException(org.springframework.http.HttpStatus.BAD_REQUEST, "WhatsApp " + fieldName + " is missing.");
		}
		return value.trim();
	}
}
