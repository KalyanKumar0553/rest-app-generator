package com.src.main.cdn.service;

import java.time.OffsetDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.src.main.cdn.dto.CdnImageBatchSettingsResponseDTO;
import com.src.main.cdn.model.CdnImageUploadSettingsEntity;
import com.src.main.cdn.repository.CdnImageUploadSettingsRepository;

@Service
public class CdnImageUploadSettingsService {

	private final CdnImageUploadSettingsRepository settingsRepository;

	public CdnImageUploadSettingsService(CdnImageUploadSettingsRepository settingsRepository) {
		this.settingsRepository = settingsRepository;
	}

	@Transactional(readOnly = true)
	public boolean isBatchProcessingEnabled() {
		return getOrCreate().isBatchProcessingEnabled();
	}

	@Transactional(readOnly = true)
	public CdnImageBatchSettingsResponseDTO getSettings() {
		CdnImageUploadSettingsEntity settings = getOrCreate();
		return new CdnImageBatchSettingsResponseDTO(
				settings.isBatchProcessingEnabled(),
				settings.getUpdatedByUserId(),
				settings.getUpdatedAt());
	}

	@Transactional
	public CdnImageBatchSettingsResponseDTO updateBatchProcessing(boolean enabled, String actor) {
		CdnImageUploadSettingsEntity settings = getOrCreate();
		settings.setBatchProcessingEnabled(enabled);
		settings.setUpdatedByUserId(normalizeActor(actor));
		settings.setUpdatedAt(OffsetDateTime.now());
		settingsRepository.save(settings);
		return new CdnImageBatchSettingsResponseDTO(
				settings.isBatchProcessingEnabled(),
				settings.getUpdatedByUserId(),
				settings.getUpdatedAt());
	}

	private CdnImageUploadSettingsEntity getOrCreate() {
		return settingsRepository.findById(CdnImageUploadSettingsEntity.SINGLETON_ID)
				.orElseGet(() -> {
					CdnImageUploadSettingsEntity entity = new CdnImageUploadSettingsEntity();
					entity.setId(CdnImageUploadSettingsEntity.SINGLETON_ID);
					entity.setBatchProcessingEnabled(true);
					entity.setUpdatedByUserId("system");
					entity.setUpdatedAt(OffsetDateTime.now());
					return settingsRepository.save(entity);
				});
	}

	private String normalizeActor(String actor) {
		return actor == null || actor.isBlank() ? "system" : actor.trim();
	}
}
