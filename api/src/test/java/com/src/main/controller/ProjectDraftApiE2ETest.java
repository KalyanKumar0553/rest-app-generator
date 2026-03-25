package com.src.main.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.security.Principal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.src.main.auth.service.RbacService;
import com.src.main.dto.ProjectTabDefinitionDTO;
import com.src.main.exception.GlobalExceptionHandler;
import com.src.main.model.PluginModuleEntity;
import com.src.main.model.ProjectEntity;
import com.src.main.repository.ProjectCollaborationRequestRepository;
import com.src.main.repository.ProjectContributorRepository;
import com.src.main.repository.ProjectDraftVersionRepository;
import com.src.main.repository.PluginModuleRepository;
import com.src.main.repository.ProjectRepository;
import com.src.main.repository.ProjectRunRepository;
import com.src.main.service.ProjectDraftService;
import com.src.main.service.ProjectDraftSpecMapperService;
import com.src.main.service.ProjectCollaborationService;
import com.src.main.service.ProjectEventStreamService;
import com.src.main.service.ProjectNameValidationService;
import com.src.main.service.ProjectOrchestrationService;
import com.src.main.service.ProjectService;
import com.src.main.service.ProjectServiceImpl;
import com.src.main.service.ProjectTabDefinitionService;
import com.src.main.service.ProjectUserIdentityService;
import com.src.main.service.ProjectYamlService;
import com.src.main.testsupport.ProjectDraftFixtures;

import jakarta.validation.Validation;

@ExtendWith(MockitoExtension.class)
class ProjectDraftApiE2ETest {

	@Mock
	private ProjectRepository projectRepository;
	@Mock
	private ProjectRunRepository projectRunRepository;
	@Mock
	private ProjectContributorRepository projectContributorRepository;
	@Mock
	private ProjectCollaborationRequestRepository projectCollaborationRequestRepository;
	@Mock
	private ProjectDraftVersionRepository projectDraftVersionRepository;
	@Mock
	private PluginModuleRepository pluginModuleRepository;
	@Mock
	private ProjectUserIdentityService projectUserIdentityService;
	@Mock
	private ProjectNameValidationService projectNameValidationService;
	@Mock
	private ProjectCollaborationService projectCollaborationService;
	@Mock
	private RbacService rbacService;
	@Mock
	private ProjectOrchestrationService projectOrchestrationService;
	@Mock
	private ProjectEventStreamService projectEventStreamService;

	private MockMvc mockMvc;
	private ObjectMapper objectMapper;
	private ProjectTabDefinitionService projectTabDefinitionService;

	@BeforeEach
	void setUp() {
		objectMapper = new ObjectMapper();
		projectTabDefinitionService = org.mockito.Mockito.mock(ProjectTabDefinitionService.class);
		org.mockito.Mockito.lenient().when(projectTabDefinitionService.getEnabledTabs("java")).thenReturn(defaultJavaTabs());
		org.mockito.Mockito.lenient().when(projectTabDefinitionService.getEnabledTabs("node")).thenReturn(defaultNodeTabs());
		ProjectService projectService = new ProjectServiceImpl(
				projectRepository,
				projectRunRepository,
				projectCollaborationRequestRepository,
				projectContributorRepository,
				projectDraftVersionRepository,
				pluginModuleRepository,
				projectUserIdentityService,
				new ProjectYamlService(),
				new ProjectDraftService(objectMapper, projectTabDefinitionService),
				new ProjectDraftSpecMapperService(),
				projectNameValidationService,
				projectCollaborationService,
				rbacService,
				Validation.buildDefaultValidatorFactory().getValidator());

		ProjectController controller = new ProjectController(
				projectService,
				projectOrchestrationService,
				projectEventStreamService,
				new ProjectCollaborationService(projectEventStreamService),
				projectUserIdentityService);

		LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
		validator.afterPropertiesSet();

		mockMvc = MockMvcBuilders.standaloneSetup(controller)
				.setControllerAdvice(new GlobalExceptionHandler())
				.setValidator(validator)
				.setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
				.build();
	}

	@Test
	void createDraft_withValidPayload_returnsProjectIdAndPersistsGeneratedYaml() throws Exception {
		when(rbacService.currentUserHasPermission("project.create")).thenReturn(true);
		when(projectUserIdentityService.currentUserId(any(Principal.class))).thenReturn("user-1");
		when(projectUserIdentityService.resolve("user-1"))
				.thenReturn(new ProjectUserIdentityService.ResolvedProjectUser("user-1", linkedKeys("user-1")));
		when(projectRepository.saveAndFlush(any(ProjectEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

		mockMvc.perform(post("/api/projects/draft")
						.principal(() -> "user-1")
						.contentType(APPLICATION_JSON)
						.content(objectMapper.writeValueAsBytes(ProjectDraftFixtures.mapOf(
								"draftData", ProjectDraftFixtures.minimalJavaDraft(),
								"draftVersion", 5))))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.projectId").isNotEmpty());
	}

	@Test
	void createDraft_withEmptyPayload_returnsBadRequest() throws Exception {
		mockMvc.perform(post("/api/projects/draft")
						.principal(() -> "user-1")
						.contentType(APPLICATION_JSON)
						.content("{}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errorMsg").value("must not be null"));
	}

	@Test
	void updateDraft_withInvalidProjectName_returnsBadRequest() throws Exception {
		UUID projectId = UUID.randomUUID();
		ProjectEntity existing = new ProjectEntity();
		existing.setId(projectId);
		existing.setOwnerId("user-1");
		existing.setName("Old");
		existing.setCreatedAt(OffsetDateTime.now().minusDays(1));

		when(rbacService.currentUserHasPermission("project.update")).thenReturn(true);
		when(projectUserIdentityService.currentUserId(any(Principal.class))).thenReturn("user-1");
		when(projectUserIdentityService.resolve("user-1"))
				.thenReturn(new ProjectUserIdentityService.ResolvedProjectUser("user-1", linkedKeys("user-1")));
		when(projectRepository.findWithContributorsByIdForUpdate(projectId)).thenReturn(Optional.of(existing));

		mockMvc.perform(put("/api/projects/{projectId}/draft", projectId)
						.principal(() -> "user-1")
						.contentType(APPLICATION_JSON)
						.content(objectMapper.writeValueAsBytes(ProjectDraftFixtures.mapOf(
								"draftData", ProjectDraftFixtures.invalidDraftMissingProjectName(),
								"draftVersion", 2))))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errorMsg").value("Project name is required."));
	}

	@Test
	void getTabDetails_withNodeGenerator_returnsNodeTabs() throws Exception {
		mockMvc.perform(get("/api/projects/tab-details")
						.param("generator", "node"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].key").value("general"))
				.andExpect(jsonPath("$[1].key").value("entities"))
				.andExpect(jsonPath("$[*].key", hasItem("controllers")));
	}

	@Test
	void getTabDetails_onlyReturnsModuleTabWhenModuleConfigIsEnabledInRegistry() throws Exception {
		PluginModuleEntity authModule = new PluginModuleEntity();
		authModule.setId(UUID.randomUUID());
		authModule.setCode("auth");
		authModule.setName("Authentication");
		authModule.setEnabled(true);
		authModule.setEnableConfig(true);
		when(pluginModuleRepository.findAllByOrderByNameAsc()).thenReturn(List.of(authModule));

		mockMvc.perform(get("/api/projects/tab-details")
						.param("generator", "java")
						.param("dependency", "auth"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[*].key", hasItem("auth")));
	}

	private Set<String> linkedKeys(String userId) {
		Set<String> keys = new LinkedHashSet<>();
		keys.add(userId);
		return keys;
	}

	private List<ProjectTabDefinitionDTO> defaultJavaTabs() {
		return List.of(
				new ProjectTabDefinitionDTO("general", "General", "public", "java-general", 10),
				new ProjectTabDefinitionDTO("actuator", "Actuator", "device_hub", "actuator", 20),
				new ProjectTabDefinitionDTO("entities", "Entities", "storage", "entities", 30),
				new ProjectTabDefinitionDTO("data-objects", "Data Objects", "category", "data-objects", 40),
				new ProjectTabDefinitionDTO("mappers", "Mappers", "shuffle", "mappers", 50),
				new ProjectTabDefinitionDTO("modules", "Modules", "widgets", "modules", 60),
				new ProjectTabDefinitionDTO("auth", "Auth", "lock", "module-auth", 70),
				new ProjectTabDefinitionDTO("controllers", "Controllers", "tune", "controllers", 80));
	}

	private List<ProjectTabDefinitionDTO> defaultNodeTabs() {
		return List.of(
				new ProjectTabDefinitionDTO("general", "General", "public", "node-general", 10),
				new ProjectTabDefinitionDTO("entities", "Entities", "storage", "entities", 20),
				new ProjectTabDefinitionDTO("data-objects", "Data Objects", "category", "data-objects", 30),
				new ProjectTabDefinitionDTO("mappers", "Mappers", "shuffle", "mappers", 40),
				new ProjectTabDefinitionDTO("modules", "Modules", "widgets", "modules", 50),
				new ProjectTabDefinitionDTO("controllers", "Controllers", "tune", "controllers", 60));
	}
}
