package com.src.main.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.src.main.dto.ProjectDraftResponseDTO;
import com.src.main.dto.ProjectDraftUpsertRequestDTO;
import com.src.main.dto.ProjectTabDefinitionDTO;
import com.src.main.exception.GenericException;
import com.src.main.model.ProjectEntity;
import com.src.main.repository.ProjectCollaborationRequestRepository;
import com.src.main.repository.ProjectContributorRepository;
import com.src.main.repository.ProjectDraftVersionRepository;
import com.src.main.repository.PluginModuleRepository;
import com.src.main.repository.ProjectRepository;
import com.src.main.repository.ProjectRunRepository;
import com.src.main.testsupport.ProjectDraftFixtures;

import jakarta.validation.Validation;
import jakarta.validation.Validator;

@ExtendWith(MockitoExtension.class)
class ProjectServiceImplDraftTest {

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
	private com.src.main.auth.service.RbacService rbacService;
	@Captor
	private ArgumentCaptor<ProjectEntity> projectCaptor;

	private ProjectYamlService projectYamlService;
	private ProjectDraftService projectDraftService;
	private ProjectDraftSpecMapperService projectDraftSpecMapperService;
	private ProjectTabDefinitionService projectTabDefinitionService;
	private Validator validator;
	private ProjectServiceImpl service;

	@BeforeEach
	void setUp() {
		projectYamlService = new ProjectYamlService();
		projectTabDefinitionService = org.mockito.Mockito.mock(ProjectTabDefinitionService.class);
		org.mockito.Mockito.lenient().when(projectTabDefinitionService.getEnabledTabs("java")).thenReturn(defaultJavaTabs());
		org.mockito.Mockito.lenient().when(projectTabDefinitionService.getEnabledTabs("node")).thenReturn(defaultNodeTabs());
		projectDraftService = new ProjectDraftService(new com.fasterxml.jackson.databind.ObjectMapper(), projectTabDefinitionService);
		projectDraftSpecMapperService = new ProjectDraftSpecMapperService();
		validator = Validation.buildDefaultValidatorFactory().getValidator();
		service = new ProjectServiceImpl(
				projectRepository,
				projectRunRepository,
				projectCollaborationRequestRepository,
				projectContributorRepository,
				projectDraftVersionRepository,
				pluginModuleRepository,
				projectUserIdentityService,
				projectYamlService,
				projectDraftService,
				projectDraftSpecMapperService,
				projectNameValidationService,
				projectCollaborationService,
				rbacService,
				validator);
	}

	@Test
	void createDraft_withValidDraft_persistsBackendGeneratedYamlAndMetadata() {
		ProjectDraftUpsertRequestDTO request = new ProjectDraftUpsertRequestDTO();
		request.setDraftData(ProjectDraftFixtures.minimalJavaDraft());
		request.setDraftVersion(3);

		when(rbacService.currentUserHasPermission("project.create")).thenReturn(true);
		when(projectUserIdentityService.resolve("user-1"))
				.thenReturn(new ProjectUserIdentityService.ResolvedProjectUser("user-1", linkedKeys("user-1")));
		when(projectRepository.saveAndFlush(any(ProjectEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

		ProjectDraftResponseDTO response = service.createDraft(request, "user-1");

		assertThat(response.getProjectId()).isNotBlank();
		verify(projectNameValidationService).ensureUniqueProjectName(eq("Customer API"), any(), eq(null));
		verify(projectRepository).saveAndFlush(projectCaptor.capture());
		ProjectEntity saved = projectCaptor.getValue();
		assertThat(saved.getName()).isEqualTo("Customer API");
		assertThat(saved.getArtifact()).isEqualTo("customer-api");
		assertThat(saved.getGenerator()).isEqualTo("java");
		assertThat(saved.getDraftVersion()).isEqualTo(3);
		assertThat(saved.getDraftData()).contains("\"projectName\":\"Customer API\"");
		assertThat(saved.getYaml()).contains("artifactId: \"customer-api\"");
		assertThat(saved.getYaml()).contains("rest-spec:");
		assertThat(saved.getCreatedAt()).isNotNull();
		assertThat(saved.getUpdatedAt()).isNotNull();
	}

	@Test
	void createDraft_withEmptyDraftData_returnsBadRequest() {
		ProjectDraftUpsertRequestDTO request = new ProjectDraftUpsertRequestDTO();

		when(rbacService.currentUserHasPermission("project.create")).thenReturn(true);

		assertThatThrownBy(() -> service.createDraft(request, "user-1"))
				.isInstanceOf(GenericException.class)
				.extracting(ex -> ((GenericException) ex).getErrorMsg())
				.isEqualTo("draftData must be provided");
		verify(projectRepository, never()).saveAndFlush(any());
	}

	@Test
	void updateDraft_withValidDraft_replacesPersistedYamlFromBackendMapper() {
		UUID projectId = UUID.randomUUID();
		ProjectDraftUpsertRequestDTO request = new ProjectDraftUpsertRequestDTO();
		request.setDraftData(ProjectDraftFixtures.minimalNodeDraft());
		request.setDraftVersion(7);

		ProjectEntity existing = new ProjectEntity();
		existing.setId(projectId);
		existing.setOwnerId("user-1");
		existing.setName("Old Name");
		existing.setDraftVersion(7);
		existing.setCreatedAt(OffsetDateTime.now().minusDays(1));

		when(rbacService.currentUserHasPermission("project.update")).thenReturn(true);
		when(projectUserIdentityService.resolve("user-1"))
				.thenReturn(new ProjectUserIdentityService.ResolvedProjectUser("user-1", linkedKeys("user-1")));
		when(projectRepository.findWithContributorsByIdForUpdate(projectId)).thenReturn(Optional.of(existing));
		when(projectRepository.saveAndFlush(any(ProjectEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

		service.updateDraft(projectId, request, "user-1");

		assertThat(existing.getName()).isEqualTo("Node API");
		assertThat(existing.getGenerator()).isEqualTo("node");
		assertThat(existing.getDraftVersion()).isEqualTo(8);
		assertThat(existing.getYaml()).contains("generator: \"node\"");
		assertThat(existing.getYaml()).contains("packageManager: \"pnpm\"");
		assertThat(existing.getUpdatedAt()).isNotNull();
		verify(projectRepository).saveAndFlush(existing);
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
				new ProjectTabDefinitionDTO("controllers", "Controllers", "tune", "controllers", 70));
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
