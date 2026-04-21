package com.src.main.communication.service;

import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.src.main.communication.model.EmailMessageRequest;
import com.src.main.communication.model.SmsMessageRequest;
import com.src.main.communication.model.WhatsAppMessageRequest;
import com.src.main.communication.service.sender.CommunicationMessageSenderRegistry;
import com.src.main.communication.service.sender.EmailMessageSender;
import com.src.main.communication.service.sender.SmsMessageSender;
import com.src.main.communication.service.sender.WhatsAppMessageSender;

@Service
public class MsgService {

	private final TemplateEngine templateEngine;
	private final CommunicationMessageSenderRegistry senderRegistry;

	public MsgService(TemplateEngine templateEngine, CommunicationMessageSenderRegistry senderRegistry) {
		this.templateEngine = templateEngine;
		this.senderRegistry = senderRegistry;
	}

	public void sendHtmlEmail(String sender, String recipient, String subject, String templateName, Context context) {
		String html = templateEngine.process(templateName, context);
		sendEmail(sender, recipient, subject, html);
	}

	public void sendEmail(String sender, String recipient, String subject, String html) {
		senderRegistry.resolve(com.src.main.communication.model.CommunicationChannelType.EMAIL)
				.send(new EmailMessageRequest(sender, recipient, subject, html));
	}

	public void sendSMS(String sender, String recipient, String msgTxt) {
		senderRegistry.resolve(com.src.main.communication.model.CommunicationChannelType.SMS)
				.send(new SmsMessageRequest(sender, recipient, msgTxt));
	}

	public void sendWhatsApp(String recipient, String message) {
		senderRegistry.resolve(com.src.main.communication.model.CommunicationChannelType.WHATSAPP)
				.send(new WhatsAppMessageRequest(recipient, message));
	}
}
