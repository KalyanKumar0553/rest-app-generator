package com.src.main.service;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.ResponseFormat;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.yaml.snakeyaml.Yaml;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.src.main.dto.AiLabsGenerateResponseDTO;
import com.src.main.dto.AiLabsJobStatusDTO;
import com.src.main.dto.AiLabsStepDTO;
import com.src.main.dto.ProjectDraftResponseDTO;
import com.src.main.dto.ProjectDraftUpsertRequestDTO;
import com.src.main.exception.GenericException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.ai.openai", name = "enabled", havingValue = "true")
public class AiLabsService {

	private static final Pattern YAML_FENCE_PATTERN = Pattern.compile("```(?:yaml|yml)?\\s*(.*?)```", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
	private static final Pattern JSON_FENCE_PATTERN = Pattern.compile("```(?:json)?\\s*(.*?)```", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
	private static final String GENERIC_AI_GENERATION_ERROR = "Error while generating the Project. Please try again";
	private static final String STATUS_PENDING = "PENDING";
	private static final String STATUS_IN_PROGRESS = "IN_PROGRESS";
	private static final String STATUS_COMPLETED = "COMPLETED";
	private static final String STATUS_FAILED = "FAILED";
	private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {
	};
	private static final ResponseFormat AI_DRAFT_RESPONSE_FORMAT = ResponseFormat.builder()
			.type(ResponseFormat.Type.JSON_OBJECT)
			.build();

	private final AiLabsEventStreamService eventStreamService;
	private final ProjectService projectService;
	private final ProjectDraftSpecMapperService projectDraftSpecMapperService;
	private final ProjectNameValidationService projectNameValidationService;
	private final ProjectUserIdentityService projectUserIdentityService;
	private final ChatClient.Builder chatClientBuilder;
	private final ObjectMapper objectMapper;

	@Value("${app.ai.openai.enabled:false}")
	private boolean openAiEnabled;

	@Value("${app.ai.openai.model:gpt-4o-mini}")
	private String openAiModel;

	private final ConcurrentMap<UUID, JobState> jobs = new ConcurrentHashMap<>();

	public AiLabsGenerateResponseDTO createJob(String prompt, String ownerId) {
		if (!openAiEnabled) {
			throw new GenericException(HttpStatus.BAD_REQUEST, "AI Labs is not configured for this environment.");
		}
		UUID jobId = UUID.randomUUID();
		JobState state = JobState.create(jobId, prompt);
		jobs.put(jobId, state);
		publish(state);
		SecurityContext securityContext = snapshotSecurityContext();
		CompletableFuture.runAsync(() -> runJobWithSecurityContext(state, ownerId, securityContext));
		return new AiLabsGenerateResponseDTO(jobId, state.status());
	}

	public AiLabsJobStatusDTO getJob(UUID jobId) {
		JobState state = jobs.get(jobId);
		if (state == null) {
			throw new IllegalArgumentException("AI Labs job not found: " + jobId);
		}
		return state.snapshot();
	}

	public org.springframework.web.servlet.mvc.method.annotation.SseEmitter subscribe(UUID jobId) {
		return eventStreamService.subscribe(jobId, getJob(jobId));
	}

	private void runJobWithSecurityContext(JobState state, String ownerId, SecurityContext securityContext) {
		SecurityContext previousContext = SecurityContextHolder.getContext();
		try {
			SecurityContextHolder.setContext(securityContext);
			runJob(state, ownerId);
		} finally {
			SecurityContextHolder.clearContext();
			if (previousContext != null && previousContext.getAuthentication() != null) {
				SecurityContextHolder.setContext(previousContext);
			}
		}
	}

	private void runJob(JobState state, String ownerId) {
		try {
			updateStep(state, "contact_openai", STATUS_IN_PROGRESS, "Opening Spring AI stream to OpenAI.");
			Map<String, Object> draftData = requestDraftFromOpenAi(state);
			updateStep(state, "contact_openai", STATUS_COMPLETED, "Received a complete streamed project plan from OpenAI.");

			updateStep(state, "build_spec", STATUS_IN_PROGRESS, "Normalizing the generated project draft.");
			Map<String, Object> normalizedDraft = normalizeDraftData(draftData, state.prompt(), ownerId);
			updateStep(state, "build_spec", STATUS_COMPLETED, "Prepared project draft data.");

			updateStep(state, "validate_spec", STATUS_IN_PROGRESS, "Validating the generated project spec.");
			projectDraftSpecMapperService.buildSpec(normalizedDraft);
			updateStep(state, "validate_spec", STATUS_COMPLETED, "Validated the generated project spec.");

			updateStep(state, "save_project", STATUS_IN_PROGRESS, "Saving your generated project.");
			ProjectDraftUpsertRequestDTO request = new ProjectDraftUpsertRequestDTO();
			request.setDraftData(normalizedDraft);
			ProjectDraftResponseDTO response = projectService.createDraft(request, ownerId);
			String generator = resolveGenerator(normalizedDraft);
			updateStep(state, "save_project", STATUS_COMPLETED, "Project saved successfully.");
			state.complete(response.getProjectId(), generator);
			publish(state);
		} catch (Exception ex) {
			String message = resolveUserFacingErrorMessage(ex);
			state.fail(message);
			updateStep(state, findCurrentStepKey(state), STATUS_FAILED, message);
			publish(state);
		}
	}

	private Map<String, Object> requestDraftFromOpenAi(JobState state) throws Exception {
		ChatClient chatClient = chatClientBuilder
				.defaultOptions(OpenAiChatOptions.builder()
						.model(openAiModel)
						.temperature(0.1d)
						.responseFormat(AI_DRAFT_RESPONSE_FORMAT)
						.build())
				.build();
		StringBuilder contentBuffer = new StringBuilder();
		AtomicInteger chunkCounter = new AtomicInteger();
		chatClient.prompt()
				.system(systemPrompt())
				.user(state.prompt())
				.stream()
				.content()
				.doOnNext(chunk -> {
					if (chunk == null || chunk.isBlank()) {
						return;
					}
					contentBuffer.append(chunk);
					state.updateStreamPreview(contentBuffer.toString());
					int index = chunkCounter.incrementAndGet();
					if (index == 1 || index % 12 == 0) {
						updateStep(state, "contact_openai", STATUS_IN_PROGRESS,
								"Streaming AI response... " + contentBuffer.length() + " characters received.");
					}
				})
				.blockLast();
		String text = contentBuffer.toString();
		return parseDraftPayload(text);
	}

	private String systemPrompt() {
		return """
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
	}

	private Map<String, Object> parseDraftPayload(String text) {
		if (text == null || text.isBlank()) {
			throw new IllegalArgumentException("OpenAI returned an empty project draft.");
		}
		String trimmed = text.trim();
		try {
			return objectMapper.readValue(trimmed, MAP_TYPE);
		} catch (Exception ignored) {
			Matcher jsonMatcher = JSON_FENCE_PATTERN.matcher(trimmed);
			if (jsonMatcher.find()) {
				try {
					return objectMapper.readValue(jsonMatcher.group(1).trim(), MAP_TYPE);
				} catch (Exception innerIgnored) {
					// Fall through to YAML compatibility parsing.
				}
			}
		}
		try {
			String yamlText = trimmed;
			Matcher yamlMatcher = YAML_FENCE_PATTERN.matcher(trimmed);
			if (yamlMatcher.find()) {
				yamlText = yamlMatcher.group(1).trim();
			}
			Object parsed = new Yaml().load(yamlText);
			if (!(parsed instanceof Map<?, ?> map)) {
				throw new IllegalArgumentException("AI output must be a JSON or YAML object at the root.");
			}
			return new LinkedHashMap<>((Map<String, Object>) map);
		} catch (IllegalArgumentException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new IllegalArgumentException("AI returned invalid structured draft output.");
		}
	}

	private String resolveUserFacingErrorMessage(Exception ex) {
		if (ex instanceof GenericException genericException) {
			return genericException.getMessage();
		}
		String message = ex == null ? "" : String.valueOf(ex.getMessage()).trim();
		if (message.contains("empty project draft")
				|| message.contains("invalid structured draft output")
				|| message.contains("invalid json")
				|| message.contains("invalid yaml")
				|| message.contains("must be a JSON or YAML object")
				|| message.contains("must be a YAML object")) {
			return GENERIC_AI_GENERATION_ERROR;
		}
		return "Failed to generate project from AI prompt.";
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> normalizeDraftData(Map<String, Object> draftData, String prompt, String ownerId) {
		Map<String, Object> normalized = draftData == null ? new LinkedHashMap<>() : new LinkedHashMap<>(draftData);
		Map<String, Object> settings = normalized.get("settings") instanceof Map<?, ?> map
				? new LinkedHashMap<>((Map<String, Object>) map)
				: new LinkedHashMap<>();
		String language = stringValue(settings.get("language"));
		if (language.isBlank()) {
			String promptText = prompt == null ? "" : prompt.toLowerCase();
			language = promptText.contains("python") ? "python" : (promptText.contains("node") ? "node" : "java");
		}
		settings.put("language", language);
		if (stringValue(settings.get("projectGroup")).isBlank()) {
			settings.put("projectGroup", "io.bootrid");
		}
		if (stringValue(settings.get("projectDescription")).isBlank()) {
			settings.put("projectDescription", prompt.trim());
		}
		if (stringValue(settings.get("projectName")).isBlank()) {
			settings.put("projectName", deriveProjectName(prompt));
		}
		if ("node".equalsIgnoreCase(language)) {
			if (stringValue(settings.get("packageManager")).isBlank()) {
				settings.put("packageManager", "npm");
			}
			if (settings.get("serverPort") == null) {
				settings.put("serverPort", 3000);
			}
		} else if ("python".equalsIgnoreCase(language)) {
			settings.remove("packageManager");
			if (settings.get("serverPort") == null) {
				settings.put("serverPort", 8000);
			}
		} else if (stringValue(settings.get("buildType")).isBlank()) {
			settings.put("buildType", "gradle");
		}
		settings.put("projectName", uniqueProjectName(stringValue(settings.get("projectName")), ownerId));
		normalized.put("settings", settings);

		normalized.computeIfAbsent("database", ignored -> new LinkedHashMap<>());
		normalized.computeIfAbsent("preferences", ignored -> new LinkedHashMap<>());
		normalized.computeIfAbsent("entities", ignored -> new ArrayList<>());
		normalized.computeIfAbsent("relations", ignored -> new ArrayList<>());
		normalized.computeIfAbsent("dataObjects", ignored -> new ArrayList<>());
		normalized.computeIfAbsent("enums", ignored -> new ArrayList<>());
		normalized.computeIfAbsent("mappers", ignored -> new ArrayList<>());
		normalized.computeIfAbsent("controllers", ignored -> new LinkedHashMap<>());
		normalized.computeIfAbsent("selectedDependencies", ignored -> new ArrayList<>());
		return normalized;
	}

	@Transactional(readOnly = true)
	protected String uniqueProjectName(String baseName, String ownerId) {
		String trimmedBase = stringValue(baseName).isBlank() ? "ai-generated-project" : baseName.trim();
		ProjectUserIdentityService.ResolvedProjectUser currentUser = projectUserIdentityService.resolve(ownerId);
		String candidate = trimmedBase;
		int suffix = 2;
		while (true) {
			try {
				projectNameValidationService.ensureUniqueProjectName(candidate, currentUser, null);
				return candidate;
			} catch (GenericException ex) {
				if (ex.getMessage() == null || !ex.getMessage().contains("already exists")) {
					throw ex;
				}
				candidate = trimmedBase + "-" + suffix++;
			}
		}
	}

	private String deriveProjectName(String prompt) {
		String base = stringValue(prompt).toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("^-+|-+$", "");
		if (base.isBlank()) {
			return "ai-generated-project";
		}
		String[] parts = base.split("-");
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < Math.min(parts.length, 4); i++) {
			if (parts[i].isBlank()) {
				continue;
			}
			if (builder.length() > 0) {
				builder.append('-');
			}
			builder.append(parts[i]);
		}
		return builder.isEmpty() ? "ai-generated-project" : builder.toString();
	}

	private String resolveGenerator(Map<String, Object> draftData) {
		Object settings = draftData.get("settings");
		if (settings instanceof Map<?, ?> map) {
			return stringValue(((Map<?, ?>) map).get("language"));
		}
		return "java";
	}

	private SecurityContext snapshotSecurityContext() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		SecurityContext context = SecurityContextHolder.createEmptyContext();
		context.setAuthentication(authentication);
		return context;
	}

	private String stringValue(Object value) {
		return value == null ? "" : String.valueOf(value).trim();
	}

	private void updateStep(JobState state, String stepKey, String status, String message) {
		state.updateStep(stepKey, status, message);
		publish(state);
	}

	private void publish(JobState state) {
		eventStreamService.publish(state.jobId(), state.snapshot());
	}

	private String findCurrentStepKey(JobState state) {
		return state.steps().stream()
				.filter(step -> STATUS_IN_PROGRESS.equals(step.getStatus()))
				.map(AiLabsStepDTO::getKey)
				.findFirst()
				.orElse("save_project");
	}

	private static final class JobState {
		private final UUID jobId;
		private final String prompt;
		private final List<AiLabsStepDTO> steps;
		private final OffsetDateTime createdAt;
		private OffsetDateTime updatedAt;
		private String status;
		private String streamPreview;
		private String projectId;
		private String generator;
		private String errorMessage;

		private JobState(UUID jobId, String prompt, List<AiLabsStepDTO> steps, OffsetDateTime createdAt, OffsetDateTime updatedAt,
				String status, String streamPreview, String projectId, String generator, String errorMessage) {
			this.jobId = jobId;
			this.prompt = prompt;
			this.steps = steps;
			this.createdAt = createdAt;
			this.updatedAt = updatedAt;
			this.status = status;
			this.streamPreview = streamPreview;
			this.projectId = projectId;
			this.generator = generator;
			this.errorMessage = errorMessage;
		}

		private static JobState create(UUID jobId, String prompt) {
			OffsetDateTime now = OffsetDateTime.now();
			List<AiLabsStepDTO> steps = new ArrayList<>();
			steps.add(new AiLabsStepDTO("contact_openai", "Contact OpenAI", STATUS_PENDING, "Waiting to start.", now));
			steps.add(new AiLabsStepDTO("build_spec", "Build Draft", STATUS_PENDING, "Waiting to start.", now));
			steps.add(new AiLabsStepDTO("validate_spec", "Validate Spec", STATUS_PENDING, "Waiting to start.", now));
			steps.add(new AiLabsStepDTO("save_project", "Save Project", STATUS_PENDING, "Waiting to start.", now));
			return new JobState(jobId, prompt, steps, now, now, STATUS_IN_PROGRESS, "", null, null, null);
		}

		private void updateStep(String stepKey, String status, String message) {
			OffsetDateTime now = OffsetDateTime.now();
			for (AiLabsStepDTO step : steps) {
				if (step.getKey().equals(stepKey)) {
					step.setStatus(status);
					step.setMessage(message);
					step.setUpdatedAt(now);
					break;
				}
			}
			this.status = STATUS_FAILED.equals(status) ? STATUS_FAILED : STATUS_IN_PROGRESS;
			this.updatedAt = now;
			if (STATUS_FAILED.equals(status)) {
				this.errorMessage = message;
			}
		}

		private void complete(String projectId, String generator) {
			this.status = STATUS_COMPLETED;
			this.projectId = projectId;
			this.generator = generator;
			this.updatedAt = OffsetDateTime.now();
		}

		private void fail(String message) {
			this.status = STATUS_FAILED;
			this.errorMessage = message;
			this.updatedAt = OffsetDateTime.now();
		}

		private AiLabsJobStatusDTO snapshot() {
			List<AiLabsStepDTO> stepCopies = steps.stream()
					.map(step -> new AiLabsStepDTO(step.getKey(), step.getLabel(), step.getStatus(), step.getMessage(), step.getUpdatedAt()))
					.toList();
			return new AiLabsJobStatusDTO(jobId, status, prompt, stepCopies, streamPreview, projectId, generator, errorMessage, createdAt, updatedAt);
		}

		private UUID jobId() {
			return jobId;
		}

		private String prompt() {
			return prompt;
		}

		private List<AiLabsStepDTO> steps() {
			return steps;
		}

		private String status() {
			return status;
		}

		private void updateStreamPreview(String text) {
			if (text == null) {
				return;
			}
			this.streamPreview = text.length() > 4000 ? text.substring(0, 4000) : text;
			this.updatedAt = OffsetDateTime.now();
		}
	}
}
