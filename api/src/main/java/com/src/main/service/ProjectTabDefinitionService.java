package com.src.main.service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.src.main.dto.ProjectTabDefinitionAdminRequestDTO;
import com.src.main.dto.ProjectTabDefinitionAdminResponseDTO;
import com.src.main.dto.ProjectTabDefinitionDTO;
import com.src.main.exception.GenericException;
import com.src.main.model.ProjectTabDefinitionEntity;
import com.src.main.repository.ProjectTabDefinitionRepository;

@Service
public class ProjectTabDefinitionService {

	private static final Set<String> ALLOWED_GENERATORS = Set.of("java", "kotlin", "node", "python");

	private final ProjectTabDefinitionRepository projectTabDefinitionRepository;

	public ProjectTabDefinitionService(ProjectTabDefinitionRepository projectTabDefinitionRepository) {
		this.projectTabDefinitionRepository = projectTabDefinitionRepository;
	}

	@Transactional(readOnly = true)
	public List<ProjectTabDefinitionDTO> getEnabledTabs(String generatorLanguage) {
		String normalizedGenerator = normalizeGeneratorLanguage(generatorLanguage);
		return projectTabDefinitionRepository
				.findByGeneratorLanguageIgnoreCaseAndEnabledTrueOrderByDisplayOrderAscTabKeyAsc(normalizedGenerator)
				.stream()
				.map(this::toProjectTabDefinitionDto)
				.toList();
	}

	@Transactional(readOnly = true)
	public List<ProjectTabDefinitionAdminResponseDTO> getAdminTabs() {
		return projectTabDefinitionRepository.findAllByOrderByGeneratorLanguageAscDisplayOrderAscTabKeyAsc().stream()
				.map(this::toAdminResponseDto)
				.toList();
	}

	@Transactional
	public ProjectTabDefinitionAdminResponseDTO create(ProjectTabDefinitionAdminRequestDTO request, String userId) {
		validateRequest(request, null);
		ProjectTabDefinitionEntity entity = new ProjectTabDefinitionEntity();
		entity.setId(UUID.randomUUID());
		apply(entity, request, userId, true);
		return toAdminResponseDto(projectTabDefinitionRepository.save(entity));
	}

	@Transactional
	public ProjectTabDefinitionAdminResponseDTO update(UUID id, ProjectTabDefinitionAdminRequestDTO request, String userId) {
		ProjectTabDefinitionEntity entity = projectTabDefinitionRepository.findById(id)
				.orElseThrow(() -> new GenericException(HttpStatus.NOT_FOUND, "Project tab definition not found."));
		validateRequest(request, id);
		apply(entity, request, userId, false);
		return toAdminResponseDto(projectTabDefinitionRepository.save(entity));
	}

	@Transactional
	public void delete(UUID id) {
		ProjectTabDefinitionEntity entity = projectTabDefinitionRepository.findById(id)
				.orElseThrow(() -> new GenericException(HttpStatus.NOT_FOUND, "Project tab definition not found."));
		projectTabDefinitionRepository.delete(entity);
	}

	private void validateRequest(ProjectTabDefinitionAdminRequestDTO request, UUID existingId) {
		String key = normalizeValue(request.getKey());
		String label = normalizeValue(request.getLabel());
		String icon = normalizeValue(request.getIcon());
		String componentKey = normalizeValue(request.getComponentKey());
		String generatorLanguage = normalizeGeneratorLanguage(request.getGeneratorLanguage());
		Integer order = request.getOrder();

		if (key == null) {
			throw new GenericException(HttpStatus.BAD_REQUEST, "Tab key is required.");
		}
		if (label == null) {
			throw new GenericException(HttpStatus.BAD_REQUEST, "Tab label is required.");
		}
		if (icon == null) {
			throw new GenericException(HttpStatus.BAD_REQUEST, "Tab icon is required.");
		}
		if (componentKey == null) {
			throw new GenericException(HttpStatus.BAD_REQUEST, "Component key is required.");
		}
		if (order == null || order < 0) {
			throw new GenericException(HttpStatus.BAD_REQUEST, "Display order must be zero or greater.");
		}

		projectTabDefinitionRepository.findByGeneratorLanguageIgnoreCaseAndTabKeyIgnoreCase(generatorLanguage, key)
				.filter(existing -> existingId == null || !existing.getId().equals(existingId))
				.ifPresent(existing -> {
					throw new GenericException(HttpStatus.CONFLICT, "A tab with this key already exists for the selected language.");
				});
	}

	private void apply(ProjectTabDefinitionEntity entity, ProjectTabDefinitionAdminRequestDTO request, String userId, boolean creating) {
		OffsetDateTime now = OffsetDateTime.now();
		entity.setTabKey(normalizeValue(request.getKey()));
		entity.setLabel(normalizeValue(request.getLabel()));
		entity.setIcon(normalizeValue(request.getIcon()));
		entity.setComponentKey(normalizeValue(request.getComponentKey()));
		entity.setDisplayOrder(request.getOrder() == null ? 0 : request.getOrder());
		entity.setGeneratorLanguage(normalizeGeneratorLanguage(request.getGeneratorLanguage()));
		entity.setEnabled(request.getEnabled() == null || request.getEnabled());
		if (creating) {
			entity.setCreatedAt(now);
			entity.setCreatedByUserId(normalizeCreatedBy(userId));
		}
		entity.setUpdatedAt(now);
	}

	private ProjectTabDefinitionDTO toProjectTabDefinitionDto(ProjectTabDefinitionEntity entity) {
		return new ProjectTabDefinitionDTO(
				entity.getTabKey(),
				entity.getLabel(),
				entity.getIcon(),
				entity.getComponentKey(),
				entity.getDisplayOrder());
	}

	private ProjectTabDefinitionAdminResponseDTO toAdminResponseDto(ProjectTabDefinitionEntity entity) {
		return new ProjectTabDefinitionAdminResponseDTO(
				entity.getId(),
				entity.getTabKey(),
				entity.getLabel(),
				entity.getIcon(),
				entity.getComponentKey(),
				entity.getDisplayOrder(),
				entity.getGeneratorLanguage(),
				entity.isEnabled(),
				entity.getCreatedAt(),
				entity.getUpdatedAt());
	}

	private String normalizeGeneratorLanguage(String value) {
		String normalized = normalizeValue(value);
		if (normalized == null) {
			return "java";
		}
		String lower = normalized.toLowerCase(Locale.ROOT);
		if (!ALLOWED_GENERATORS.contains(lower)) {
			throw new GenericException(HttpStatus.BAD_REQUEST, "Unsupported generator language: " + value);
		}
		return lower;
	}

	private String normalizeValue(String value) {
		if (value == null) {
			return null;
		}
		String normalized = value.trim();
		return normalized.isEmpty() ? null : normalized;
	}

	private String normalizeCreatedBy(String userId) {
		String normalized = normalizeValue(userId);
		return normalized == null ? "system" : normalized;
	}
}
