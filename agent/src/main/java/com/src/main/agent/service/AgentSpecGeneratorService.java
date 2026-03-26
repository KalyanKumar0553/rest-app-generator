package com.src.main.agent.service;

import java.util.List;
import java.util.Map;

import com.src.main.agent.model.AgentMessageEntity;

public interface AgentSpecGeneratorService {

	String generateAgentReply(String systemPrompt, List<AgentMessageEntity> conversationHistory, String userMessage);

	Map<String, Object> generateSpec(String systemPrompt, List<AgentMessageEntity> conversationHistory);
}
