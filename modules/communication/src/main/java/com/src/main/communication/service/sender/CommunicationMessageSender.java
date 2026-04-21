package com.src.main.communication.service.sender;

import com.src.main.communication.model.CommunicationChannelType;

public interface CommunicationMessageSender<T> {
	CommunicationChannelType channelType();
	void send(T request);
}
