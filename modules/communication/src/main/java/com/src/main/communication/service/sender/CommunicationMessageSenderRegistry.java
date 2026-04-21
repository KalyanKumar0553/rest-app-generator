package com.src.main.communication.service.sender;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.src.main.communication.model.CommunicationChannelType;

@Component
public class CommunicationMessageSenderRegistry {
	private final Map<CommunicationChannelType, CommunicationMessageSender<?>> senders = new EnumMap<>(CommunicationChannelType.class);

	public CommunicationMessageSenderRegistry(List<CommunicationMessageSender<?>> senderList) {
		for (CommunicationMessageSender<?> sender : senderList) {
			senders.put(sender.channelType(), sender);
		}
	}

	@SuppressWarnings("unchecked")
	public <T> CommunicationMessageSender<T> resolve(CommunicationChannelType channelType) {
		CommunicationMessageSender<?> sender = senders.get(channelType);
		if (sender == null) {
			throw new IllegalStateException("No message sender registered for " + channelType);
		}
		return (CommunicationMessageSender<T>) sender;
	}
}
