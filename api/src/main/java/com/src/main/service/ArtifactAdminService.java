package com.src.main.service;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.src.main.dto.ArtifactAppRequestDTO;
import com.src.main.dto.ArtifactAppResponseDTO;
import com.src.main.dto.ArtifactAppVersionResponseDTO;
import com.src.main.model.ArtifactAppEntity;
import com.src.main.model.ArtifactAppVersionEntity;
import com.src.main.repository.ArtifactAppRepository;
import com.src.main.repository.ArtifactAppVersionRepository;

@Service
public class ArtifactAdminService {

	private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {
	};
	private static final TypeReference<List<String>> STRING_LIST_TYPE = new TypeReference<>() {
	};
	private static final DateTimeFormatter VERSION_SUFFIX_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
	private static final List<String> ALLOWED_PACKS = List.of("crm", "order", "inventory", "shipping");

	private final ArtifactAppRepository artifactAppRepository;
	private final ArtifactAppVersionRepository artifactAppVersionRepository;
	private final ObjectMapper objectMapper;

	public ArtifactAdminService(
			ArtifactAppRepository artifactAppRepository,
			ArtifactAppVersionRepository artifactAppVersionRepository,
			ObjectMapper objectMapper) {
		this.artifactAppRepository = artifactAppRepository;
		this.artifactAppVersionRepository = artifactAppVersionRepository;
		this.objectMapper = objectMapper;
	}

	@Transactional(readOnly = true)
	public List<ArtifactAppResponseDTO> listApps() {
		return artifactAppRepository.findAll().stream()
				.map(this::toResponse)
				.toList();
	}

	@Transactional(readOnly = true)
	public ArtifactAppResponseDTO getApp(UUID appId) {
		return toResponse(getEntity(appId));
	}

	@Transactional(readOnly = true)
	public List<ArtifactAppVersionResponseDTO> listVersions(UUID appId) {
		ArtifactAppEntity app = getEntity(appId);
		return artifactAppVersionRepository.findByAppOrderByCreatedAtDesc(app).stream()
				.map(this::toVersionResponse)
				.toList();
	}

	@Transactional
	public ArtifactAppResponseDTO createApp(ArtifactAppRequestDTO request, String ownerUserId) {
		String normalizedCode = normalizeCode(request.getCode());
		artifactAppRepository.findByCodeIgnoreCase(normalizedCode).ifPresent(existing -> {
			throw new IllegalArgumentException("Artifact app code already exists: " + normalizedCode);
		});
		ArtifactAppEntity entity = new ArtifactAppEntity();
		apply(entity, request, ownerUserId, true);
		return toResponse(artifactAppRepository.save(entity));
	}

	@Transactional
	public ArtifactAppResponseDTO updateApp(UUID appId, ArtifactAppRequestDTO request, String ownerUserId) {
		ArtifactAppEntity entity = getEntity(appId);
		String normalizedCode = normalizeCode(request.getCode());
		artifactAppRepository.findByCodeIgnoreCase(normalizedCode)
				.filter(existing -> !existing.getId().equals(appId))
				.ifPresent(existing -> {
					throw new IllegalArgumentException("Artifact app code already exists: " + normalizedCode);
				});
		apply(entity, request, ownerUserId, false);
		return toResponse(artifactAppRepository.save(entity));
	}

	@Transactional
	public ArtifactAppVersionResponseDTO createVersion(UUID appId, String requestedVersionCode, String userId) {
		ArtifactAppEntity app = getEntity(appId);
		String resolvedVersionCode = normalizeVersionCode(requestedVersionCode);
		if (resolvedVersionCode.isBlank()) {
			resolvedVersionCode = buildDefaultVersionCode();
		}
		final String versionCode = resolvedVersionCode;
		artifactAppVersionRepository.findByAppAndVersionCodeIgnoreCase(app, versionCode).ifPresent(existing -> {
			throw new IllegalArgumentException("Version already exists: " + versionCode);
		});
		ArtifactAppVersionEntity version = new ArtifactAppVersionEntity();
		version.setApp(app);
		version.setVersionCode(versionCode);
		version.setConfigJson(app.getConfigJson());
		version.setPublished(false);
		version.setCreatedByUserId(userId);
		return toVersionResponse(artifactAppVersionRepository.save(version));
	}

	@Transactional
	public ArtifactAppResponseDTO publish(UUID appId, String requestedVersionCode, String userId) {
		ArtifactAppEntity app = getEntity(appId);
		ArtifactAppVersionEntity version = resolveOrCreateVersion(app, requestedVersionCode, userId);
		List<ArtifactAppVersionEntity> versions = artifactAppVersionRepository.findByAppOrderByCreatedAtDesc(app);
		for (ArtifactAppVersionEntity current : versions) {
			current.setPublished(current.getId().equals(version.getId()));
		}
		artifactAppVersionRepository.saveAll(versions);
		app.setPublishedVersion(version.getVersionCode());
		return toResponse(artifactAppRepository.save(app));
	}

	private ArtifactAppVersionEntity resolveOrCreateVersion(ArtifactAppEntity app, String requestedVersionCode, String userId) {
		String versionCode = normalizeVersionCode(requestedVersionCode);
		if (!versionCode.isBlank()) {
			return artifactAppVersionRepository.findByAppAndVersionCodeIgnoreCase(app, versionCode)
					.orElseThrow(() -> new IllegalArgumentException("Version not found: " + versionCode));
		}
		return artifactAppVersionRepository.save(buildVersion(app, buildDefaultVersionCode(), userId));
	}

	private ArtifactAppVersionEntity buildVersion(ArtifactAppEntity app, String versionCode, String userId) {
		ArtifactAppVersionEntity version = new ArtifactAppVersionEntity();
		version.setApp(app);
		version.setVersionCode(versionCode);
		version.setConfigJson(app.getConfigJson());
		version.setPublished(false);
		version.setCreatedByUserId(userId);
		return version;
	}

	private void apply(ArtifactAppEntity entity, ArtifactAppRequestDTO request, String ownerUserId, boolean assignOwner) {
		List<String> normalizedPacks = normalizePacks(request.getEnabledPacks());
		entity.setCode(normalizeCode(request.getCode()));
		entity.setName(trimToNull(request.getName()));
		entity.setDescription(trimToNull(request.getDescription()));
		entity.setStatus(normalizeStatus(request.getStatus()));
		entity.setGeneratorLanguage(normalizeRuntimeValue(request.getGeneratorLanguage(), "java"));
		entity.setBuildTool(normalizeRuntimeValue(request.getBuildTool(), "maven"));
		entity.setEnabledPacksJson(writeJson(normalizedPacks));
		entity.setConfigJson(writeJson(normalizeConfig(request.getConfig(), normalizedPacks)));
		if (assignOwner || entity.getOwnerUserId() == null || entity.getOwnerUserId().isBlank()) {
			entity.setOwnerUserId(ownerUserId);
		}
	}

	private Map<String, Object> normalizeConfig(Map<String, Object> config, List<String> enabledPacks) {
		Map<String, Object> normalized = new LinkedHashMap<>(config == null ? Map.of() : config);
		Map<String, Object> modules = normalized.get("modules") instanceof Map<?, ?> existing
				? new LinkedHashMap<>((Map<String, Object>) existing)
				: new LinkedHashMap<>();
		for (String pack : ALLOWED_PACKS) {
			Map<String, Object> moduleConfig = modules.get(pack) instanceof Map<?, ?> value
					? new LinkedHashMap<>((Map<String, Object>) value)
					: new LinkedHashMap<>();
			moduleConfig.put("enabled", enabledPacks.contains(pack));
			modules.put(pack, moduleConfig);
		}
		normalized.put("modules", modules);
		return normalized;
	}

	private List<String> normalizePacks(List<String> requestedPacks) {
		LinkedHashSet<String> normalized = new LinkedHashSet<>();
		if (requestedPacks != null) {
			for (String value : requestedPacks) {
				String pack = value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
				if (pack.isBlank()) {
					continue;
				}
				if (!ALLOWED_PACKS.contains(pack)) {
					throw new IllegalArgumentException("Unsupported pack: " + pack);
				}
				normalized.add(pack);
			}
		}
		if (normalized.contains("shipping") && !normalized.contains("order")) {
			throw new IllegalArgumentException("Shipping requires the order pack.");
		}
		if (normalized.contains("inventory") && !normalized.contains("order")) {
			throw new IllegalArgumentException("Inventory requires the order pack.");
		}
		return List.copyOf(normalized);
	}

	private String normalizeCode(String value) {
		String normalized = trimToNull(value);
		if (normalized == null) {
			throw new IllegalArgumentException("Code is required");
		}
		normalized = normalized.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9-]+", "-").replaceAll("-{2,}", "-");
		normalized = normalized.replaceAll("^-|-$", "");
		if (normalized.isBlank()) {
			throw new IllegalArgumentException("Code must contain letters or digits");
		}
		return normalized;
	}

	private String normalizeStatus(String value) {
		String normalized = trimToNull(value);
		return normalized == null ? "DRAFT" : normalized.toUpperCase(Locale.ROOT);
	}

	private String normalizeRuntimeValue(String value, String fallback) {
		String normalized = trimToNull(value);
		return normalized == null ? fallback : normalized.toLowerCase(Locale.ROOT);
	}

	private String normalizeVersionCode(String value) {
		String normalized = trimToNull(value);
		return normalized == null ? "" : normalized;
	}

	private String buildDefaultVersionCode() {
		return "v" + VERSION_SUFFIX_FORMAT.format(OffsetDateTime.now());
	}

	private String trimToNull(String value) {
		if (value == null) {
			return null;
		}
		String trimmed = value.trim();
		return trimmed.isBlank() ? null : trimmed;
	}

	private ArtifactAppEntity getEntity(UUID appId) {
		return artifactAppRepository.findById(appId)
				.orElseThrow(() -> new IllegalArgumentException("Artifact app not found: " + appId));
	}

	private ArtifactAppResponseDTO toResponse(ArtifactAppEntity entity) {
		List<ArtifactAppVersionEntity> versions = artifactAppVersionRepository.findByAppOrderByCreatedAtDesc(entity);
		return new ArtifactAppResponseDTO(
				entity.getId(),
				entity.getCode(),
				entity.getName(),
				entity.getDescription(),
				entity.getStatus(),
				entity.getOwnerUserId(),
				entity.getGeneratorLanguage(),
				entity.getBuildTool(),
				readStringList(entity.getEnabledPacksJson()),
				readMap(entity.getConfigJson()),
				entity.getPublishedVersion(),
				versions.size(),
				entity.getCreatedAt(),
				entity.getUpdatedAt());
	}

	private ArtifactAppVersionResponseDTO toVersionResponse(ArtifactAppVersionEntity entity) {
		return new ArtifactAppVersionResponseDTO(
				entity.getId(),
				entity.getVersionCode(),
				entity.isPublished(),
				entity.getCreatedByUserId(),
				readMap(entity.getConfigJson()),
				entity.getCreatedAt(),
				entity.getUpdatedAt());
	}

	private String writeJson(Object value) {
		try {
			return objectMapper.writeValueAsString(value == null ? Map.of() : value);
		} catch (IOException ex) {
			throw new IllegalArgumentException("Failed to serialize artifact configuration", ex);
		}
	}

	private Map<String, Object> readMap(String value) {
		if (value == null || value.isBlank()) {
			return Map.of();
		}
		try {
			return objectMapper.readValue(value, MAP_TYPE);
		} catch (IOException ex) {
			throw new IllegalArgumentException("Failed to read stored artifact config", ex);
		}
	}

	private List<String> readStringList(String value) {
		if (value == null || value.isBlank()) {
			return List.of();
		}
		try {
			return objectMapper.readValue(value, STRING_LIST_TYPE);
		} catch (IOException ex) {
			throw new IllegalArgumentException("Failed to read stored enabled packs", ex);
		}
	}
}
