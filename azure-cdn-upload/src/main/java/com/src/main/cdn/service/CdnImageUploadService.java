package com.src.main.cdn.service;

import java.security.MessageDigest;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.src.main.cdn.config.AzureCdnStorageProperties;
import com.src.main.cdn.dto.CdnImageDraftResponseDTO;
import com.src.main.cdn.dto.CdnImageTriggerResponseDTO;
import com.src.main.cdn.dto.CdnImageUploadResponseDTO;
import com.src.main.cdn.model.CdnImageAssetEntity;
import com.src.main.cdn.model.CdnImageUploadDraftEntity;
import com.src.main.cdn.model.CdnImageUploadStatus;
import com.src.main.cdn.repository.CdnImageAssetRepository;
import com.src.main.cdn.repository.CdnImageUploadDraftRepository;

@Service
public class CdnImageUploadService {

	private static final Logger log = LoggerFactory.getLogger(CdnImageUploadService.class);
	private static final String STORAGE_PROVIDER = "AZURE_CDN";

	private final CdnImageUploadDraftRepository draftRepository;
	private final CdnImageAssetRepository assetRepository;
	private final CdnImageUploadSettingsService settingsService;
	private final AzureCdnBlobUploadClient uploadClient;
	private final AzureCdnStorageProperties properties;
	private final AtomicBoolean workerRunning = new AtomicBoolean(false);

	public CdnImageUploadService(
			CdnImageUploadDraftRepository draftRepository,
			CdnImageAssetRepository assetRepository,
			CdnImageUploadSettingsService settingsService,
			AzureCdnBlobUploadClient uploadClient,
			AzureCdnStorageProperties properties) {
		this.draftRepository = draftRepository;
		this.assetRepository = assetRepository;
		this.settingsService = settingsService;
		this.uploadClient = uploadClient;
		this.properties = properties;
	}

	@Transactional
	public CdnImageUploadResponseDTO createDrafts(List<MultipartFile> files, String actor) {
		if (files == null || files.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "At least one image file is required.");
		}
		List<CdnImageDraftResponseDTO> drafts = new ArrayList<>();
		for (MultipartFile file : files) {
			validateFile(file);
			try {
				OffsetDateTime now = OffsetDateTime.now();
				CdnImageUploadDraftEntity entity = new CdnImageUploadDraftEntity();
				entity.setId(UUID.randomUUID());
				entity.setFileName(sanitizeFileName(file.getOriginalFilename()));
				entity.setContentType(normalizeContentType(file.getContentType()));
				entity.setSizeBytes(file.getSize());
				entity.setChecksumSha256(hash(file.getBytes()));
				entity.setBinaryData(file.getBytes());
				entity.setStatus(CdnImageUploadStatus.DRAFT);
				entity.setStorageProvider(STORAGE_PROVIDER);
				entity.setCreatedByUserId(normalizeActor(actor));
				entity.setUpdatedByUserId(normalizeActor(actor));
				entity.setCreatedAt(now);
				entity.setUpdatedAt(now);
				entity.setAttemptCount(0);
				draftRepository.save(entity);
				drafts.add(toDraftResponse(entity));
			} catch (Exception ex) {
				throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to store image draft.", ex);
			}
		}
		return new CdnImageUploadResponseDTO(drafts);
	}

	@Transactional(readOnly = true)
	public List<CdnImageDraftResponseDTO> listDrafts() {
		return draftRepository.findTop100ByOrderByCreatedAtDesc().stream()
				.map(this::toDraftResponse)
				.toList();
	}

	@Transactional
	public CdnImageTriggerResponseDTO queueDrafts(Collection<UUID> draftIds, String actor) {
		List<CdnImageUploadDraftEntity> drafts = resolveDrafts(draftIds);
		OffsetDateTime now = OffsetDateTime.now();
		List<UUID> queuedIds = new ArrayList<>();
		for (CdnImageUploadDraftEntity draft : drafts) {
			if (draft.getStatus() == CdnImageUploadStatus.COMPLETED) {
				continue;
			}
			draft.setStatus(CdnImageUploadStatus.PENDING);
			draft.setQueuedAt(now);
			draft.setUpdatedAt(now);
			draft.setUpdatedByUserId(normalizeActor(actor));
			draft.setLastErrorMessage(null);
			queuedIds.add(draft.getId());
		}
		if (!queuedIds.isEmpty()) {
			draftRepository.saveAll(drafts);
		}
		return new CdnImageTriggerResponseDTO(queuedIds, queuedIds.size());
	}

	@Scheduled(fixedDelayString = "${app.azure.cdn.scheduler-fixed-delay-ms:10000}")
	public void processPendingUploads() {
		if (!properties.isEnabled()) {
			return;
		}
		if (!settingsService.isBatchProcessingEnabled()) {
			return;
		}
		processNextPendingUpload();
	}

	public void processNextPendingUpload() {
		if (!workerRunning.compareAndSet(false, true)) {
			return;
		}
		try {
			Optional<CdnImageUploadDraftEntity> claimed = claimNextPendingDraft();
			if (claimed.isEmpty()) {
				return;
			}
			CdnImageUploadDraftEntity draft = claimed.get();
			try {
				AzureCdnBlobUploadClient.UploadResult upload = uploadClient.upload(
						draft.getBinaryData(),
						draft.getFileName(),
						draft.getContentType());
				markCompleted(draft.getId(), upload);
			} catch (Exception ex) {
				log.error("Azure CDN upload failed for draft {}", draft.getId(), ex);
				markFailed(draft.getId(), ex.getMessage());
			}
		} finally {
			workerRunning.set(false);
		}
	}

	@Transactional
	protected Optional<CdnImageUploadDraftEntity> claimNextPendingDraft() {
		Optional<CdnImageUploadDraftEntity> draftOptional = draftRepository.lockNextPendingDraft();
		draftOptional.ifPresent(draft -> {
			draft.setStatus(CdnImageUploadStatus.PROCESSING);
			draft.setProcessingStartedAt(OffsetDateTime.now());
			draft.setUpdatedAt(OffsetDateTime.now());
			draft.setUpdatedByUserId("system");
			draft.setAttemptCount(draft.getAttemptCount() + 1);
			draftRepository.save(draft);
		});
		return draftOptional;
	}

	@Transactional
	protected void markCompleted(UUID draftId, AzureCdnBlobUploadClient.UploadResult upload) {
		CdnImageUploadDraftEntity draft = draftRepository.findById(draftId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Draft not found."));
		OffsetDateTime now = OffsetDateTime.now();
		CdnImageAssetEntity asset = assetRepository.findByDraftId(draftId).orElseGet(CdnImageAssetEntity::new);
		if (asset.getId() == null) {
			asset.setId(UUID.randomUUID());
			asset.setCreatedAt(draft.getCreatedAt());
			asset.setCreatedByUserId(draft.getCreatedByUserId());
		}
		asset.setDraftId(draft.getId());
		asset.setFileName(draft.getFileName());
		asset.setContentType(draft.getContentType());
		asset.setSizeBytes(draft.getSizeBytes());
		asset.setChecksumSha256(draft.getChecksumSha256());
		asset.setStorageProvider(STORAGE_PROVIDER);
		asset.setContainerName(upload.containerName());
		asset.setBlobName(upload.blobName());
		asset.setImageUrl(upload.imageUrl());
		asset.setUploadedByUserId("system");
		asset.setUploadedAt(now);
		assetRepository.save(asset);

		draft.setStatus(CdnImageUploadStatus.COMPLETED);
		draft.setBinaryData(null);
		draft.setCompletedAt(now);
		draft.setUpdatedAt(now);
		draft.setUpdatedByUserId("system");
		draft.setLastErrorMessage(null);
		draftRepository.save(draft);
	}

	@Transactional
	protected void markFailed(UUID draftId, String errorMessage) {
		CdnImageUploadDraftEntity draft = draftRepository.findById(draftId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Draft not found."));
		draft.setStatus(CdnImageUploadStatus.FAILED);
		draft.setUpdatedAt(OffsetDateTime.now());
		draft.setUpdatedByUserId("system");
		draft.setLastErrorMessage(trimMessage(errorMessage));
		draftRepository.save(draft);
	}

	private List<CdnImageUploadDraftEntity> resolveDrafts(Collection<UUID> draftIds) {
		if (draftIds == null || draftIds.isEmpty()) {
			return draftRepository.findTop100ByOrderByCreatedAtDesc().stream()
					.filter(draft -> draft.getStatus() == CdnImageUploadStatus.DRAFT
							|| draft.getStatus() == CdnImageUploadStatus.FAILED)
					.toList();
		}
		List<CdnImageUploadDraftEntity> drafts = draftRepository.findByIdIn(draftIds);
		if (drafts.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No image drafts found for the provided ids.");
		}
		return drafts;
	}

	private CdnImageDraftResponseDTO toDraftResponse(CdnImageUploadDraftEntity draft) {
		String imageUrl = assetRepository.findByDraftId(draft.getId()).map(CdnImageAssetEntity::getImageUrl).orElse(null);
		return new CdnImageDraftResponseDTO(
				draft.getId(),
				draft.getFileName(),
				draft.getContentType(),
				draft.getSizeBytes(),
				draft.getStatus(),
				draft.getAttemptCount(),
				draft.getLastErrorMessage(),
				draft.getCreatedAt(),
				draft.getUpdatedAt(),
				draft.getQueuedAt(),
				draft.getCompletedAt(),
				imageUrl);
	}

	private void validateFile(MultipartFile file) {
		if (file == null || file.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Image file is required.");
		}
		if (file.getSize() > properties.getMaxFileSizeBytes()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Image file exceeds the configured size limit.");
		}
		String contentType = normalizeContentType(file.getContentType());
		if (!contentType.toLowerCase(Locale.ROOT).startsWith("image/")) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only image uploads are supported.");
		}
	}

	private String normalizeContentType(String contentType) {
		return (contentType == null || contentType.isBlank()) ? "application/octet-stream" : contentType.trim();
	}

	private String sanitizeFileName(String originalFilename) {
		String safe = originalFilename == null ? "image.bin" : originalFilename.replaceAll("[\\\\/]+", "-").trim();
		return safe.isBlank() ? "image.bin" : safe;
	}

	private String hash(byte[] bytes) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			return HexFormat.of().formatHex(digest.digest(bytes));
		} catch (Exception ex) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to hash image data.", ex);
		}
	}

	private String trimMessage(String message) {
		if (message == null || message.isBlank()) {
			return "Upload failed.";
		}
		return message.length() > 1000 ? message.substring(0, 1000) : message;
	}

	private String normalizeActor(String actor) {
		return actor == null || actor.isBlank() ? "system" : actor.trim();
	}
}
