package com.src.main.communication.service.sender;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;

import com.src.main.communication.model.CommunicationChannelType;

public class DefaultCommunicationConfigProvider implements CommunicationConfigProvider {
	private final ChannelConfig emailConfig;
	private final ChannelConfig smsConfig;
	private final ChannelConfig whatsappConfig;

	public DefaultCommunicationConfigProvider(
			@Value("${azure.communication.email.endpoint:}") String emailEndpoint,
			@Value("${azure.communication.email.access-key:}") String emailAccessKey,
			@Value("${twilio.communication.ssid:}") String smsSenderId,
			@Value("${twilio.communication.token:}") String smsAuthToken,
			@Value("${communication.whatsapp.connection-string:}") String whatsappConnectionString,
			@Value("${communication.whatsapp.channel-registration-id:}") String whatsappChannelRegistrationId) {
		this.emailConfig = new ChannelConfig(emailEndpoint, emailAccessKey, null, null, null);
		this.smsConfig = new ChannelConfig(null, null, smsSenderId, null, smsAuthToken);
		this.whatsappConfig = new ChannelConfig(null, null, null, whatsappChannelRegistrationId, whatsappConnectionString);
	}

	@Override
	public Optional<ChannelConfig> getEnabledConfig(CommunicationChannelType channelType) {
		return switch (channelType) {
			case EMAIL -> Optional.of(emailConfig);
			case SMS -> Optional.of(smsConfig);
			case WHATSAPP -> Optional.of(whatsappConfig);
		};
	}
}
