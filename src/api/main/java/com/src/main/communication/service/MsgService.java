package com.src.main.communication.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.azure.communication.email.EmailClient;
import com.azure.communication.email.EmailClientBuilder;
import com.azure.communication.email.models.EmailMessage;
import com.azure.communication.email.models.EmailSendResult;
import com.azure.communication.email.models.EmailSendStatus;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.SyncPoller;
import com.src.main.communication.exception.CommunicationException;
import com.src.main.common.util.RequestStatus;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;

@Service
public class MsgService {

	@Value("${azure.communication.email.endpoint}")
	private String endpoint;

	@Value("${azure.communication.email.access-key}")
	private String accessKey;

	@Value("${twilio.communication.ssid}")
	private String ACCOUNT_SID;

	@Value("${twilio.communication.token}")
	private String AUTH_TOKEN;

	private final TemplateEngine templateEngine;

	private EmailClient getEmailClient() {
		return new EmailClientBuilder()
				.endpoint(endpoint)
				.credential(new AzureKeyCredential(accessKey))
				.buildClient();
	}

	public MsgService(TemplateEngine templateEngine) {
		this.templateEngine = templateEngine;
	}

	public void sendHtmlEmail(String sender, String recipient, String subject, String templateName, Context context) {
		String html = templateEngine.process(templateName, context);
		sendEmail(sender, recipient, subject, html);
	}

	public void sendEmail(String sender, String recipient, String subject, String html) {
		EmailMessage message = new EmailMessage()
				.setSenderAddress(sender)
				.setToRecipients(recipient)
				.setSubject(subject)
				.setBodyPlainText("")
				.setBodyHtml(html);
		SyncPoller<EmailSendResult, EmailSendResult> poller = getEmailClient().beginSend(message, null);
		PollResponse<EmailSendResult> response = poller.waitForCompletion();
		if (response.getValue().getStatus() == EmailSendStatus.FAILED
				|| response.getValue().getStatus() == EmailSendStatus.CANCELED) {
			throw new CommunicationException(RequestStatus.EMAIL_SENT_ERROR);
		}
	}

	public void sendSMS(String sender, String recipient, String msgTxt) {
		Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
		Message message = Message.creator(
				new com.twilio.type.PhoneNumber(recipient),
				new com.twilio.type.PhoneNumber(sender),
				msgTxt).create();
	}
}
