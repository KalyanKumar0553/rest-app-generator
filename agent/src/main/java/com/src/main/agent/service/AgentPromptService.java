package com.src.main.agent.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.src.main.agent.model.AgentMessageEntity;

@Service
public class AgentPromptService {

	private static final String SYSTEM_PROMPT = """
			You are a project specification generator agent. Your role is to help users define REST API project specifications
			through conversational interaction.

			When the user describes their project, extract and generate a structured JSON specification with these sections:
			- settings: language (java|node|python), projectName, projectGroup, projectDescription, buildType, serverPort
			- database: type (postgresql|mysql|mongodb|h2), name
			- entities: list of entities with name, fields (name, type, primaryKey, required, unique, generationType), addRestEndpoints, addCrudOperations
			- relations: list of relations between entities (sourceEntity, targetEntity, relationType: ONE_TO_ONE|ONE_TO_MANY|MANY_TO_MANY)
			- dataObjects: list of DTOs with name and fields
			- enums: list of enums with name and values
			- controllers: custom controller configurations
			- selectedDependencies: list of dependency identifiers
			- preferences: additional project preferences

			Guidelines:
			1. Ask clarifying questions when the user's description is ambiguous.
			2. Suggest best practices (e.g., always include an id field, use appropriate data types).
			3. When you have enough information, output ONLY the JSON spec wrapped in ```json``` fences.
			4. Default to Java with Gradle if language/build is not specified.
			5. Always include a primary key field (id) for each entity.
			6. Use projectGroup "io.bootrid" as default.
			7. Keep responses concise and focused on the specification.
			""";

	public String buildSystemPrompt() {
		return SYSTEM_PROMPT;
	}

	public String buildConversationContext(List<AgentMessageEntity> messages) {
		if (messages == null || messages.isEmpty()) {
			return "";
		}
		return messages.stream()
				.map(msg -> msg.getRole().name().toLowerCase() + ": " + msg.getContent())
				.collect(Collectors.joining("\n\n"));
	}

	public String buildGenerationPrompt(List<AgentMessageEntity> messages) {
		String conversationContext = buildConversationContext(messages);
		return """
				Based on the following conversation, generate the final project specification as a JSON object.
				Output ONLY the JSON spec, no additional text or markdown fences.

				Conversation:
				%s

				Generate the complete project specification JSON now.
				""".formatted(conversationContext);
	}

	public AgentPromptService() {
	}
}
