package com.src.main.agent.service;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.src.main.agent.model.AgentMessageEntity;
import com.src.main.agent.model.AgentMessageRole;

@Service
public class DefaultAgentSpecGeneratorService implements AgentSpecGeneratorService {

	private static final Logger log = LoggerFactory.getLogger(DefaultAgentSpecGeneratorService.class);

	private final AgentPromptService agentPromptService;
	private final AgentSpecParserService agentSpecParserService;

	public DefaultAgentSpecGeneratorService(AgentPromptService agentPromptService,
			AgentSpecParserService agentSpecParserService) {
		this.agentPromptService = agentPromptService;
		this.agentSpecParserService = agentSpecParserService;
	}

	@Override
	public String generateAgentReply(String systemPrompt, List<AgentMessageEntity> conversationHistory,
			String userMessage) {
		log.debug("Generating agent reply for user message of length {}", userMessage.length());
		StringBuilder replyBuilder = new StringBuilder();
		replyBuilder.append("I understand you want to create a project. Let me help you refine the specification.\n\n");

		boolean hasEntities = conversationHistory.stream()
				.filter(msg -> msg.getRole() == AgentMessageRole.USER)
				.anyMatch(msg -> containsEntityInfo(msg.getContent()));

		if (!hasEntities && !containsEntityInfo(userMessage)) {
			replyBuilder.append("To generate a complete spec, I need more details:\n");
			replyBuilder.append("1. What entities/models does your project need?\n");
			replyBuilder.append("2. What fields should each entity have?\n");
			replyBuilder.append("3. Are there relationships between entities?\n");
			replyBuilder.append("4. Do you need any specific dependencies or features?\n");
		} else {
			replyBuilder.append("I have enough information to generate the spec. ");
			replyBuilder.append("You can ask me to generate it, or provide more details to refine.");
		}

		return replyBuilder.toString();
	}

	@Override
	public Map<String, Object> generateSpec(String systemPrompt, List<AgentMessageEntity> conversationHistory) {
		log.debug("Generating spec from {} conversation messages", conversationHistory.size());
		String generationPrompt = agentPromptService.buildGenerationPrompt(conversationHistory);

		String userDescription = extractUserDescription(conversationHistory);
		String specJson = buildSpecFromDescription(userDescription);

		return agentSpecParserService.parseSpec(specJson);
	}

	private String extractUserDescription(List<AgentMessageEntity> messages) {
		StringBuilder description = new StringBuilder();
		for (AgentMessageEntity msg : messages) {
			if (msg.getRole() == AgentMessageRole.USER) {
				if (description.length() > 0) {
					description.append(" ");
				}
				description.append(msg.getContent());
			}
		}
		return description.toString();
	}

	private String buildSpecFromDescription(String description) {
		String lowerDesc = description.toLowerCase();
		String language = lowerDesc.contains("node") ? "node"
				: lowerDesc.contains("python") ? "python" : "java";
		String projectName = deriveProjectName(description);

		StringBuilder json = new StringBuilder();
		json.append("{\n");
		json.append("  \"settings\": {\n");
		json.append("    \"language\": \"").append(language).append("\",\n");
		json.append("    \"projectName\": \"").append(escapeJson(projectName)).append("\",\n");
		json.append("    \"projectGroup\": \"io.bootrid\",\n");
		json.append("    \"projectDescription\": \"").append(escapeJson(truncate(description, 200))).append("\",\n");
		if ("java".equals(language)) {
			json.append("    \"buildType\": \"gradle\",\n");
			json.append("    \"serverPort\": 8080\n");
		} else if ("node".equals(language)) {
			json.append("    \"packageManager\": \"npm\",\n");
			json.append("    \"serverPort\": 3000\n");
		} else {
			json.append("    \"serverPort\": 8000\n");
		}
		json.append("  },\n");
		json.append("  \"database\": {},\n");
		json.append("  \"preferences\": {},\n");
		json.append("  \"entities\": [],\n");
		json.append("  \"relations\": [],\n");
		json.append("  \"dataObjects\": [],\n");
		json.append("  \"enums\": [],\n");
		json.append("  \"mappers\": [],\n");
		json.append("  \"controllers\": {},\n");
		json.append("  \"selectedDependencies\": []\n");
		json.append("}");
		return json.toString();
	}

	private String deriveProjectName(String description) {
		String base = description.toLowerCase()
				.replaceAll("[^a-z0-9]+", "-")
				.replaceAll("^-+|-+$", "");
		if (base.isBlank()) {
			return "agent-generated-project";
		}
		String[] parts = base.split("-");
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < Math.min(parts.length, 4); i++) {
			if (parts[i].isBlank()) continue;
			if (builder.length() > 0) builder.append('-');
			builder.append(parts[i]);
		}
		return builder.isEmpty() ? "agent-generated-project" : builder.toString();
	}

	private boolean containsEntityInfo(String text) {
		if (text == null) return false;
		String lower = text.toLowerCase();
		return lower.contains("entity") || lower.contains("model") || lower.contains("table")
				|| lower.contains("field") || lower.contains("column") || lower.contains("schema");
	}

	private String escapeJson(String value) {
		if (value == null) return "";
		return value.replace("\\", "\\\\")
				.replace("\"", "\\\"")
				.replace("\n", "\\n")
				.replace("\r", "\\r")
				.replace("\t", "\\t");
	}

	private String truncate(String value, int maxLength) {
		if (value == null) return "";
		return value.length() > maxLength ? value.substring(0, maxLength) : value;
	}
}
