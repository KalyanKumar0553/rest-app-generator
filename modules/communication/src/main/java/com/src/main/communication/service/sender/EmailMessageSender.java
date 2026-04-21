package com.src.main.communication.service.sender;

import org.springframework.stereotype.Component;

import com.azure.communication.email.EmailClient;
import com.azure.communication.email.EmailClientBuilder;
import com.azure.communication.email.models.EmailMessage;
import com.azure.communication.email.models.EmailSendResult;
import com.azure.communication.email.models.EmailSendStatus;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.SyncPoller;
import com.src.main.communication.exception.CommunicationException;
import com.src.main.communication.model.CommunicationChannelType;
import com.src.main.communication.model.EmailMessageRequest;
import com.src.main.common.util.RequestStatus;

@Component
public class EmailMessageSender implements CommunicationMessageSender<EmailMessageRequest> {

	private final CommunicationConfigProvider configProvider;

	public EmailMessageSender(CommunicationConfigProvider configProvider) {
		this.configProvider = configProvider;
	}

	@Override
	public CommunicationChannelType channelType() {
		return CommunicationChannelType.EMAIL;
	}

	@Override
	public void send(EmailMessageRequest request) {
		var config = configProvider.getEnabledConfig(channelType())
				.orElseThrow(() -> new CommunicationException(RequestStatus.EMAIL_SENT_ERROR));
		EmailClient client = new EmailClientBuilder()
				.endpoint(require(config.endpoint(), "email endpoint"))
				.credential(new AzureKeyCredential(require(config.accessKey(), "email access key")))
				.buildClient();
		EmailMessage message = new EmailMessage()
				.setSenderAddress(request.sender())
				.setToRecipients(request.recipient())
				.setSubject(request.subject())
				.setBodyPlainText("")
				.setBodyHtml(request.htmlBody());
		SyncPoller<EmailSendResult, EmailSendResult> poller = client.beginSend(message, null);
		PollResponse<EmailSendResult> response = poller.waitForCompletion();
		if (response.getValue().getStatus() == EmailSendStatus.FAILED
				|| response.getValue().getStatus() == EmailSendStatus.CANCELED) {
			throw new CommunicationException(RequestStatus.EMAIL_SENT_ERROR);
		}
	}

	private String require(String value, String fieldName) {
		if (value == null || value.isBlank()) {
			throw new CommunicationException(RequestStatus.EMAIL_SENT_ERROR, fieldName);
		}
		return value.trim();
	}
}
