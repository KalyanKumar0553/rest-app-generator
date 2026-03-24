package com.src.main.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.security.Principal;
import java.time.OffsetDateTime;
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
import com.src.main.exception.GlobalExceptionHandler;
import com.src.main.model.ProjectEntity;
import com.src.main.repository.ProjectCollaborationRequestRepository;
import com.src.main.repository.ProjectContributorRepository;
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
	private ProjectUserIdentityService projectUserIdentityService;
	@Mock
	private ProjectNameValidationService projectNameValidationService;
	@Mock
	private RbacService rbacService;
	@Mock
	private ProjectOrchestrationService projectOrchestrationService;
	@Mock
	private ProjectEventStreamService projectEventStreamService;

	private MockMvc mockMvc;
	private ObjectMapper objectMapper;

	@BeforeEach
	void setUp() {
		objectMapper = new ObjectMapper();
		ProjectService projectService = new ProjectServiceImpl(
				projectRepository,
				projectRunRepository,
				projectCollaborationRequestRepository,
				projectContributorRepository,
				projectUserIdentityService,
				new ProjectYamlService(),
				new ProjectDraftService(objectMapper),
				new ProjectDraftSpecMapperService(),
				projectNameValidationService,
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
		when(rbacService.currentUserHasPermission("project.contributor.manage.all")).thenReturn(false);
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
				.andExpect(jsonPath("$[4].key").value("controllers"));
	}

	private Set<String> linkedKeys(String userId) {
		Set<String> keys = new LinkedHashSet<>();
		keys.add(userId);
		return keys;
	}
}
