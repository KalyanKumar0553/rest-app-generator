package com.src.main;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * Plain Java class to test OpenAI connectivity. No Spring Boot required.
 *
 * Usage:  javac AiOpenAiConnectionTest.java && java AiOpenAiConnectionTest
 *
 * Set your API key in the OPENAI_API_KEY environment variable before running.
 */
public class AiOpenAiConnectionTest {

	private static final String OPENAI_API_KEY = System.getenv("OPENAI_API_KEY");
	private static final String MODEL = "gpt-4o-mini";
	private static final String OPENAI_URL = "https://api.openai.com/v1/chat/completions";

	private static final String SYSTEM_PROMPT = "Return only JSON that matches the provided schema. No prose. No markdown fences. "
			+ "Use only these top keys when needed: settings, database, preferences, selectedDependencies, entities, relations, dataObjects, enums, mappers, controllers. "
			+ "Default language is java unless node or python is explicitly requested. "
			+ "Omit empty sections and default values. Prefer short lists and only essential fields. "
			+ "If the request is ambiguous, choose a safe, minimal starter project.";

	private static final String USER_PROMPT = "Create a simple REST API project with a single Customer entity that has id, name, and email fields.";

	public static void main(String[] args) throws Exception {
		if (OPENAI_API_KEY == null || OPENAI_API_KEY.isBlank()) {
			System.err.println("ERROR: Set your OpenAI API key in the OPENAI_API_KEY environment variable before running.");
			System.exit(1);
		}

		System.out.println("=============================================================");
		System.out.println("  OpenAI Connection Test (Plain Java)");
		System.out.println("=============================================================");
		System.out.println("Model : " + MODEL);
		System.out.println("URL   : " + OPENAI_URL);
		System.out.println("Prompt: " + USER_PROMPT);
		System.out.println("-------------------------------------------------------------");

		String requestBody = """
				{
				  "model": "%s",
				  "temperature": 0.1,
				  "response_format": { "type": "json_object" },
				  "messages": [
				    { "role": "system", "content": "%s" },
				    { "role": "user",   "content": "%s" }
				  ]
				}
				""".formatted(MODEL, escapeJson(SYSTEM_PROMPT), escapeJson(USER_PROMPT));

		HttpClient client = HttpClient.newHttpClient();
		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(OPENAI_URL))
				.header("Content-Type", "application/json")
				.header("Authorization", "Bearer " + OPENAI_API_KEY)
				.POST(HttpRequest.BodyPublishers.ofString(requestBody))
				.build();

		System.out.println("\nSending request to OpenAI...");
		long start = System.currentTimeMillis();

		HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

		long elapsed = System.currentTimeMillis() - start;

		System.out.println("HTTP Status : " + response.statusCode());
		System.out.println("Time elapsed: " + elapsed + "ms");
		System.out.println("-------------------------------------------------------------");

		if (response.statusCode() == 200) {
			System.out.println("[PASSED] Response body:\n");
			System.out.println(response.body());
		} else if (response.statusCode() == 429) {
			System.err.println("[FAILED] 429 Too Many Requests — rate limit hit.");
			System.err.println(">>> Check your OpenAI dashboard: https://platform.openai.com/usage");
			System.err.println("Response:\n" + response.body());
		} else {
			System.err.println("[FAILED] Unexpected status: " + response.statusCode());
			System.err.println("Response:\n" + response.body());
		}

		System.out.println("\n=============================================================");
		System.out.println("  Test complete.");
		System.out.println("=============================================================");
	}

	private static String escapeJson(String text) {
		return text.replace("\\", "\\\\")
				.replace("\"", "\\\"")
				.replace("\n", "\\n")
				.replace("\r", "\\r")
				.replace("\t", "\\t");
	}
}
