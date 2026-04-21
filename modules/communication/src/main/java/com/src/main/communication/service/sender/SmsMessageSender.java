package com.src.main.communication.service.sender;

import org.springframework.stereotype.Component;

import com.src.main.communication.model.CommunicationChannelType;
import com.src.main.communication.model.SmsMessageRequest;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;

@Component
public class SmsMessageSender implements CommunicationMessageSender<SmsMessageRequest> {
	private final CommunicationConfigProvider configProvider;

	public SmsMessageSender(CommunicationConfigProvider configProvider) {
		this.configProvider = configProvider;
	}

	@Override
	public CommunicationChannelType channelType() {
		return CommunicationChannelType.SMS;
	}

	@Override
	public void send(SmsMessageRequest request) {
		var config = configProvider.getEnabledConfig(channelType())
				.orElseThrow(() -> new IllegalStateException("SMS communication config is not enabled."));
		Twilio.init(require(config.senderId(), "sms account sid"), require(config.connectionString(), "sms auth token"));
		Message.creator(
				new com.twilio.type.PhoneNumber(request.recipient()),
				new com.twilio.type.PhoneNumber(request.sender()),
				request.message()).create();
	}

	private String require(String value, String fieldName) {
		if (value == null || value.isBlank()) {
			throw new IllegalStateException(fieldName + " is required.");
		}
		return value.trim();
	}
}
