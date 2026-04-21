package com.src.main.communication.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.src.main.communication.service.sender.CommunicationConfigProvider;
import com.src.main.communication.service.sender.DefaultCommunicationConfigProvider;

@Configuration
public class CommunicationModuleConfiguration {

	@Bean
	@ConditionalOnMissingBean(CommunicationConfigProvider.class)
	public CommunicationConfigProvider communicationConfigProvider(
			@Value("${azure.communication.email.endpoint:}") String emailEndpoint,
			@Value("${azure.communication.email.access-key:}") String emailAccessKey,
			@Value("${twilio.communication.ssid:}") String smsSenderId,
			@Value("${twilio.communication.token:}") String smsAuthToken,
			@Value("${communication.whatsapp.connection-string:}") String whatsappConnectionString,
			@Value("${communication.whatsapp.channel-registration-id:}") String whatsappChannelRegistrationId) {
		return new DefaultCommunicationConfigProvider(
				emailEndpoint,
				emailAccessKey,
				smsSenderId,
				smsAuthToken,
				whatsappConnectionString,
				whatsappChannelRegistrationId);
	}
}
