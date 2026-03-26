package com.src.main;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.ResponseFormat;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * Standalone test to verify OpenAI connectivity and response.
 *
 * Usage:
 *   mvn spring-boot:run \
 *     -Dspring-boot.run.main-class=com.src.main.AiOpenAiConnectionTest \
 *     -Dspring-boot.run.arguments="--spring.ai.openai.api-key=YOUR_KEY --app.ai.openai.model=gpt-4o-mini"
 *
 * Or set env vars: APP_AI_OPENAI_API_KEY, APP_AI_OPENAI_MODEL
 */
@SpringBootApplication(exclude = {
		DataSourceAutoConfiguration.class,
		HibernateJpaAutoConfiguration.class,
		SecurityAutoConfiguration.class
})
public class AiOpenAiConnectionTest {

	private static final String TEST_PROMPT = "Create a simple REST API project with a single Customer entity that has id, name, and email fields.";

	private static final String SYSTEM_PROMPT = """
			Return only JSON that matches the provided schema. No prose. No markdown fences unless required by the client.
			Use only these top keys when needed: settings, database, preferences, selectedDependencies, entities, relations, dataObjects, enums, mappers, controllers.
			Default language is java unless node or python is explicitly requested.
			Omit empty sections and default values.
			Prefer short lists and only essential fields.
			If the request is ambiguous, choose a safe, minimal starter project.
			At minimum include:
			settings:
			  language: java|node|python
			  projectName: concise project name
			  projectGroup: io.bootrid
			  projectDescription: short description
			For entities use:
			entities:
			  - name: Customer
			    addRestEndpoints: true
			    addCrudOperations: true
			    fields:
			      - name: id
			        type: Long
			        primaryKey: true
			        generationType: IDENTITY
			      - name: email
			        type: String
			        required: true
			        unique: true
			For node projects packageManager may be npm or pnpm.
			For python projects prefer fastapi.
			""";

	public static void main(String[] args) {
		System.setProperty("spring.main.web-application-type", "none");
		SpringApplication.run(AiOpenAiConnectionTest.class, args);
	}

	@Bean
	CommandLineRunner testOpenAiConnection(ChatClient.Builder chatClientBuilder,
			@org.springframework.beans.factory.annotation.Value("${app.ai.openai.model:gpt-4o-mini}") String model) {
		return args -> {
			System.out.println("=============================================================");
			System.out.println("  OpenAI Connection Test");
			System.out.println("=============================================================");
			System.out.println("Model: " + model);
			System.out.println("Prompt: " + TEST_PROMPT);
			System.out.println("-------------------------------------------------------------");

			try {
				// --- Test 1: Non-streaming call ---
				System.out.println("\n[Test 1] Non-streaming call...");
				long start = System.currentTimeMillis();

				ChatClient chatClient = chatClientBuilder.defaultOptions(
						OpenAiChatOptions.builder()
								.model(model)
								.temperature(0.1)
								.responseFormat(ResponseFormat.builder().type(ResponseFormat.Type.JSON_OBJECT).build())
								.build()
				).build();

				String response = chatClient.prompt()
						.system(SYSTEM_PROMPT)
						.user(TEST_PROMPT)
						.call()
						.content();

				long elapsed = System.currentTimeMillis() - start;
				System.out.println("Response received in " + elapsed + "ms");
				System.out.println("Response length: " + (response == null ? 0 : response.length()) + " chars");
				System.out.println("Response preview (first 500 chars):");
				System.out.println(response == null ? "(null)" : response.substring(0, Math.min(500, response.length())));
				System.out.println("[Test 1] PASSED");

				// --- Test 2: Streaming call (mirrors AiLabsService behavior) ---
				System.out.println("\n[Test 2] Streaming call...");
				start = System.currentTimeMillis();

				StringBuilder contentBuffer = new StringBuilder();
				chatClient.prompt()
						.system(SYSTEM_PROMPT)
						.user(TEST_PROMPT)
						.stream()
						.content()
						.doOnNext(chunk -> {
							if (chunk != null && !chunk.isBlank()) {
								contentBuffer.append(chunk);
							}
						})
						.blockLast();

				elapsed = System.currentTimeMillis() - start;
				String streamedResponse = contentBuffer.toString();
				System.out.println("Streamed response received in " + elapsed + "ms");
				System.out.println("Response length: " + streamedResponse.length() + " chars");
				System.out.println("Response preview (first 500 chars):");
				System.out.println(streamedResponse.substring(0, Math.min(500, streamedResponse.length())));
				System.out.println("[Test 2] PASSED");

			} catch (Exception ex) {
				System.err.println("\n[FAILED] " + ex.getClass().getSimpleName() + ": " + ex.getMessage());
				if (ex.getMessage() != null && (ex.getMessage().contains("429") || ex.getMessage().toLowerCase().contains("too many requests"))) {
					System.err.println(">>> This is a rate-limit (429) error from OpenAI.");
					System.err.println(">>> Your API key may have exceeded its rate limit or quota.");
					System.err.println(">>> Check your OpenAI dashboard: https://platform.openai.com/usage");
				}
				ex.printStackTrace();
			}

			System.out.println("\n=============================================================");
			System.out.println("  Test complete.");
			System.out.println("=============================================================");
		};
	}
}
