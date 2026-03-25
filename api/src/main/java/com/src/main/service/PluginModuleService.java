package com.src.main.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.src.main.dto.PluginModuleResponseDTO;
import com.src.main.dto.PluginModuleSaveRequestDTO;
import com.src.main.dto.PluginModuleVersionDTO;
import com.src.main.dto.PluginModuleVersionSaveRequestDTO;
import com.src.main.exception.GenericException;
import com.src.main.model.PluginModuleEntity;
import com.src.main.model.PluginModuleVersionEntity;
import com.src.main.repository.PluginModuleRepository;
import com.src.main.repository.PluginModuleVersionRepository;

@Service
public class PluginModuleService {

	private static final String PLUGIN_FEATURE_KEY = "app.feature.plugin-modules.enabled";

	private final PluginModuleRepository pluginModuleRepository;
	private final PluginModuleVersionRepository pluginModuleVersionRepository;
	private final PluginModuleStorageService pluginModuleStorageService;
	private final ConfigMetadataService configMetadataService;
	private final ObjectMapper objectMapper;

	public PluginModuleService(
			PluginModuleRepository pluginModuleRepository,
			PluginModuleVersionRepository pluginModuleVersionRepository,
			PluginModuleStorageService pluginModuleStorageService,
			ConfigMetadataService configMetadataService,
			ObjectMapper objectMapper) {
		this.pluginModuleRepository = pluginModuleRepository;
		this.pluginModuleVersionRepository = pluginModuleVersionRepository;
		this.pluginModuleStorageService = pluginModuleStorageService;
		this.configMetadataService = configMetadataService;
		this.objectMapper = objectMapper;
	}

	@Transactional(readOnly = true)
	public List<PluginModuleResponseDTO> getAdminModules() {
		List<PluginModuleEntity> modules = pluginModuleRepository.findAllByOrderByNameAsc();
		return toResponse(modules, true);
	}

	@Transactional(readOnly = true)
	public List<PluginModuleResponseDTO> getPublishedModules(String generator) {
		if (!configMetadataService.isPropertyEnabled(PLUGIN_FEATURE_KEY, false)) {
			return Collections.emptyList();
		}
		String normalizedGenerator = generator == null ? "" : generator.trim().toLowerCase(Locale.ROOT);
		List<PluginModuleEntity> modules = pluginModuleRepository.findByEnabledTrueOrderByNameAsc();
		return toResponse(modules, false).stream()
				.filter(module -> module.currentPublishedVersionId() != null)
				.filter(module -> normalizedGenerator.isBlank()
						|| module.generatorTargets().isEmpty()
						|| module.generatorTargets().stream()
								.map(value -> value == null ? "" : value.trim().toLowerCase(Locale.ROOT))
								.anyMatch(normalizedGenerator::equals))
				.toList();
	}

	@Transactional
	public PluginModuleResponseDTO createModule(
			PluginModuleSaveRequestDTO request,
			PluginModuleVersionSaveRequestDTO versionRequest,
			MultipartFile artifact,
			String ownerUserId) {
		validateModuleRequest(request, true);
		validateVersionRequest(versionRequest);
		pluginModuleRepository.findByCodeIgnoreCase(request.getCode().trim())
				.ifPresent(existing -> {
					throw new GenericException(HttpStatus.CONFLICT, "Plugin module code already exists.");
				});

		OffsetDateTime now = OffsetDateTime.now();
		PluginModuleEntity module = new PluginModuleEntity();
		module.setId(UUID.randomUUID());
		module.setCode(normalizeCode(request.getCode()));
		module.setName(request.getName().trim());
		module.setDescription(trimToNull(request.getDescription()));
		module.setCategory(trimToNull(request.getCategory()));
		module.setEnabled(request.getEnabled() == null || request.getEnabled());
		module.setEnableConfig(request.getEnableConfig() != null && request.getEnableConfig());
		module.setGeneratorTargetsJson(writeGeneratorTargets(request.getGeneratorTargets()));
		module.setCreatedByUserId(ownerUserId);
		module.setCreatedAt(now);
		module.setUpdatedAt(now);
		pluginModuleRepository.save(module);

		PluginModuleVersionEntity version = createVersionEntity(module, versionRequest, artifact, ownerUserId, true);
		module.setCurrentPublishedVersionId(version.getId());
		module.setUpdatedAt(OffsetDateTime.now());
		pluginModuleRepository.save(module);
		return toResponse(List.of(module), true).stream().findFirst()
				.orElseThrow(() -> new GenericException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to create plugin module."));
	}

	@Transactional
	public PluginModuleResponseDTO updateModule(UUID moduleId, PluginModuleSaveRequestDTO request) {
		PluginModuleEntity module = getModule(moduleId);
		validateModuleRequest(request, false);
		String normalizedCode = normalizeCode(request.getCode());
		pluginModuleRepository.findByCodeIgnoreCase(normalizedCode)
				.filter(existing -> !existing.getId().equals(moduleId))
				.ifPresent(existing -> {
					throw new GenericException(HttpStatus.CONFLICT, "Plugin module code already exists.");
				});
		module.setCode(normalizedCode);
		module.setName(request.getName().trim());
		module.setDescription(trimToNull(request.getDescription()));
		module.setCategory(trimToNull(request.getCategory()));
		module.setEnabled(request.getEnabled() == null || request.getEnabled());
		module.setEnableConfig(request.getEnableConfig() != null && request.getEnableConfig());
		module.setGeneratorTargetsJson(writeGeneratorTargets(request.getGeneratorTargets()));
		module.setUpdatedAt(OffsetDateTime.now());
		pluginModuleRepository.save(module);
		return toResponse(List.of(module), true).stream().findFirst()
				.orElseThrow(() -> new GenericException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to update plugin module."));
	}

	@Transactional
	public PluginModuleResponseDTO uploadVersion(
			UUID moduleId,
			PluginModuleVersionSaveRequestDTO request,
			MultipartFile artifact,
			String ownerUserId) {
		PluginModuleEntity module = getModule(moduleId);
		validateVersionRequest(request);
		pluginModuleVersionRepository.findByPluginModuleIdAndVersionCodeIgnoreCase(moduleId, request.getVersionCode().trim())
				.ifPresent(existing -> {
					throw new GenericException(HttpStatus.CONFLICT, "Plugin version already exists.");
				});
		createVersionEntity(module, request, artifact, ownerUserId, false);
		module.setUpdatedAt(OffsetDateTime.now());
		pluginModuleRepository.save(module);
		return toResponse(List.of(module), true).stream().findFirst()
				.orElseThrow(() -> new GenericException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to upload plugin version."));
	}

	@Transactional
	public PluginModuleResponseDTO publishVersion(UUID moduleId, UUID versionId) {
		PluginModuleEntity module = getModule(moduleId);
		List<PluginModuleVersionEntity> versions = pluginModuleVersionRepository.findByPluginModuleIdOrderByCreatedAtDesc(moduleId);
		PluginModuleVersionEntity targetVersion = versions.stream()
				.filter(version -> version.getId().equals(versionId))
				.findFirst()
				.orElseThrow(() -> new GenericException(HttpStatus.NOT_FOUND, "Plugin version not found."));
		for (PluginModuleVersionEntity version : versions) {
			version.setPublished(version.getId().equals(versionId));
			version.setUpdatedAt(OffsetDateTime.now());
		}
		pluginModuleVersionRepository.saveAll(versions);
		module.setCurrentPublishedVersionId(targetVersion.getId());
		module.setUpdatedAt(OffsetDateTime.now());
		pluginModuleRepository.save(module);
		return toResponse(List.of(module), true).stream().findFirst()
				.orElseThrow(() -> new GenericException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to publish plugin version."));
	}

	@Transactional(readOnly = true)
	public List<Map<String, Object>> resolveSelectedPlugins(Collection<Map<String, Object>> pluginSelections) {
		if (pluginSelections == null || pluginSelections.isEmpty()) {
			return Collections.emptyList();
		}
		List<UUID> versionIds = pluginSelections.stream()
				.map(selection -> parseUuid(selection.get("versionId")))
				.filter(java.util.Objects::nonNull)
				.toList();
		if (versionIds.isEmpty()) {
			return Collections.emptyList();
		}
		Map<UUID, PluginModuleVersionEntity> versionsById = pluginModuleVersionRepository.findAllById(versionIds).stream()
				.collect(Collectors.toMap(PluginModuleVersionEntity::getId, version -> version));
		List<Map<String, Object>> resolved = new ArrayList<>();
		for (Map<String, Object> selection : pluginSelections) {
			UUID versionId = parseUuid(selection.get("versionId"));
			if (versionId == null) {
				continue;
			}
			PluginModuleVersionEntity version = versionsById.get(versionId);
			if (version == null) {
				continue;
			}
			Map<String, Object> item = new LinkedHashMap<>();
			item.put("pluginId", version.getPluginModule().getId().toString());
			item.put("versionId", version.getId().toString());
			item.put("code", version.getPluginModule().getCode());
			item.put("name", version.getPluginModule().getName());
			item.put("versionCode", version.getVersionCode());
			resolved.add(item);
		}
		return resolved;
	}

	public void applyPluginsToProject(Path projectRoot, Collection<Map<String, Object>> pluginSelections) {
		if (projectRoot == null || pluginSelections == null || pluginSelections.isEmpty()) {
			return;
		}
		List<UUID> versionIds = pluginSelections.stream()
				.map(selection -> parseUuid(selection.get("versionId")))
				.filter(java.util.Objects::nonNull)
				.toList();
		if (versionIds.isEmpty()) {
			return;
		}
		Map<UUID, PluginModuleVersionEntity> versionsById = pluginModuleVersionRepository.findAllById(versionIds).stream()
				.collect(Collectors.toMap(PluginModuleVersionEntity::getId, version -> version));
		for (Map<String, Object> selection : pluginSelections) {
			UUID versionId = parseUuid(selection.get("versionId"));
			if (versionId == null) {
				continue;
			}
			PluginModuleVersionEntity version = versionsById.get(versionId);
			if (version == null) {
				continue;
			}
			unzipArtifact(projectRoot, pluginModuleStorageService.resolve(version.getStorageKey()));
		}
	}

	private PluginModuleVersionEntity createVersionEntity(
			PluginModuleEntity module,
			PluginModuleVersionSaveRequestDTO request,
			MultipartFile artifact,
			String ownerUserId,
			boolean published) {
		PluginModuleStorageService.StoredPluginArtifact storedArtifact =
				pluginModuleStorageService.store(module.getCode(), request.getVersionCode().trim(), artifact);
		PluginModuleVersionEntity version = new PluginModuleVersionEntity();
		version.setId(UUID.randomUUID());
		version.setPluginModule(module);
		version.setVersionCode(request.getVersionCode().trim());
		version.setChangelog(trimToNull(request.getChangelog()));
		version.setFileName(storedArtifact.fileName());
		version.setStorageKey(storedArtifact.storageKey());
		version.setChecksumSha256(storedArtifact.checksumSha256());
		version.setSizeBytes(storedArtifact.sizeBytes());
		version.setPublished(published);
		version.setCreatedByUserId(ownerUserId);
		version.setCreatedAt(OffsetDateTime.now());
		version.setUpdatedAt(OffsetDateTime.now());
		return pluginModuleVersionRepository.save(version);
	}

	private List<PluginModuleResponseDTO> toResponse(List<PluginModuleEntity> modules, boolean includeAllVersions) {
		if (modules.isEmpty()) {
			return Collections.emptyList();
		}
		List<UUID> moduleIds = modules.stream().map(PluginModuleEntity::getId).toList();
		Map<UUID, List<PluginModuleVersionEntity>> versionsByModuleId = pluginModuleVersionRepository
				.findByPluginModuleIdInOrderByCreatedAtDesc(moduleIds)
				.stream()
				.collect(Collectors.groupingBy(version -> version.getPluginModule().getId(),
						LinkedHashMap::new,
						Collectors.toList()));
		return modules.stream()
				.map(module -> {
					List<PluginModuleVersionEntity> versions = versionsByModuleId.getOrDefault(module.getId(), Collections.emptyList());
					List<PluginModuleVersionDTO> versionDtos = versions.stream()
							.filter(version -> includeAllVersions || version.isPublished() || version.getId().equals(module.getCurrentPublishedVersionId()))
							.sorted(Comparator.comparing(PluginModuleVersionEntity::getCreatedAt).reversed())
							.map(this::toVersionDto)
							.toList();
					return new PluginModuleResponseDTO(
							module.getId(),
							module.getCode(),
							module.getName(),
							module.getDescription(),
							module.getCategory(),
							module.isEnabled(),
							module.isEnableConfig(),
							readGeneratorTargets(module.getGeneratorTargetsJson()),
							module.getCurrentPublishedVersionId(),
							module.getCreatedAt(),
							module.getUpdatedAt(),
							versionDtos);
				})
				.toList();
	}

	private PluginModuleVersionDTO toVersionDto(PluginModuleVersionEntity version) {
		return new PluginModuleVersionDTO(
				version.getId(),
				version.getVersionCode(),
				version.getChangelog(),
				version.getFileName(),
				version.getSizeBytes(),
				version.isPublished(),
				version.getCreatedAt(),
				version.getUpdatedAt());
	}

	private PluginModuleEntity getModule(UUID moduleId) {
		return pluginModuleRepository.findById(moduleId)
				.orElseThrow(() -> new GenericException(HttpStatus.NOT_FOUND, "Plugin module not found."));
	}

	private void validateModuleRequest(PluginModuleSaveRequestDTO request, boolean creating) {
		if (request == null) {
			throw new GenericException(HttpStatus.BAD_REQUEST, "Plugin module details are required.");
		}
		if (creating && isBlank(request.getCode())) {
			throw new GenericException(HttpStatus.BAD_REQUEST, "Plugin module code is required.");
		}
		if (isBlank(request.getName())) {
			throw new GenericException(HttpStatus.BAD_REQUEST, "Plugin module name is required.");
		}
		if (request.getGeneratorTargets() == null || request.getGeneratorTargets().isEmpty()) {
			throw new GenericException(HttpStatus.BAD_REQUEST, "At least one generator target is required.");
		}
	}

	private void validateVersionRequest(PluginModuleVersionSaveRequestDTO request) {
		if (request == null || isBlank(request.getVersionCode())) {
			throw new GenericException(HttpStatus.BAD_REQUEST, "Plugin version is required.");
		}
	}

	private String normalizeCode(String code) {
		if (isBlank(code)) {
			throw new GenericException(HttpStatus.BAD_REQUEST, "Plugin module code is required.");
		}
		return code.trim().toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9._-]+", "-");
	}

	private String writeGeneratorTargets(List<String> values) {
		LinkedHashSet<String> normalized = values == null ? new LinkedHashSet<>()
				: values.stream()
						.filter(java.util.Objects::nonNull)
						.map(value -> value.trim().toLowerCase(Locale.ROOT))
						.filter(value -> !value.isBlank())
						.collect(Collectors.toCollection(LinkedHashSet::new));
		if (normalized.isEmpty()) {
			normalized.add("java");
		}
		try {
			return objectMapper.writeValueAsString(normalized);
		} catch (JsonProcessingException ex) {
			throw new GenericException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to store plugin generator targets.");
		}
	}

	private List<String> readGeneratorTargets(String value) {
		if (value == null || value.isBlank()) {
			return List.of("java");
		}
		try {
			return objectMapper.readerForListOf(String.class).readValue(value);
		} catch (IOException ex) {
			return List.of("java");
		}
	}

	private void unzipArtifact(Path projectRoot, Path artifactPath) {
		if (!Files.exists(artifactPath)) {
			throw new GenericException(HttpStatus.NOT_FOUND, "Plugin artifact file is missing.");
		}
		try (InputStream inputStream = Files.newInputStream(artifactPath);
			 ZipInputStream zipInputStream = new ZipInputStream(inputStream)) {
			ZipEntry entry;
			while ((entry = zipInputStream.getNextEntry()) != null) {
				Path target = projectRoot.resolve(entry.getName()).normalize();
				if (!target.startsWith(projectRoot)) {
					throw new GenericException(HttpStatus.BAD_REQUEST, "Plugin artifact contains an invalid path.");
				}
				if (entry.isDirectory()) {
					Files.createDirectories(target);
				} else {
					Files.createDirectories(target.getParent());
					Files.copy(zipInputStream, target, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
				}
				zipInputStream.closeEntry();
			}
		} catch (IOException ex) {
			throw new GenericException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to apply plugin artifact to generated project.");
		}
	}

	private UUID parseUuid(Object value) {
		if (value == null) {
			return null;
		}
		try {
			return UUID.fromString(String.valueOf(value));
		} catch (IllegalArgumentException ex) {
			return null;
		}
	}

	private boolean isBlank(String value) {
		return value == null || value.trim().isEmpty();
	}

	private String trimToNull(String value) {
		if (value == null) {
			return null;
		}
		String trimmed = value.trim();
		return trimmed.isEmpty() ? null : trimmed;
	}
}
