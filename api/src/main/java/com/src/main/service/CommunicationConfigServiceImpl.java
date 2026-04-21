package com.src.main.service;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.src.main.dto.CommunicationConfigRequestDTO;
import com.src.main.dto.CommunicationConfigResponseDTO;
import com.src.main.model.CommunicationConfigEntity;
import com.src.main.repository.CommunicationConfigRepository;

@Service
public class CommunicationConfigServiceImpl implements CommunicationConfigService {
	private final CommunicationConfigRepository repository;
	private final DataEncryptionService dataEncryptionService;
	private final SecureRandom secureRandom = new SecureRandom();

	public CommunicationConfigServiceImpl(CommunicationConfigRepository repository, DataEncryptionService dataEncryptionService) {
		this.repository = repository;
		this.dataEncryptionService = dataEncryptionService;
	}

	@Override
	@Transactional
	public CommunicationConfigResponseDTO saveOrUpdate(CommunicationConfigRequestDTO request) {
		CommunicationConfigEntity entity = repository.findFirstByServiceTypeIgnoreCase(request.getServiceType().trim())
				.orElseGet(CommunicationConfigEntity::new);
		entity.setServiceType(request.getServiceType().trim().toUpperCase());
		entity.setDisplayName(request.getDisplayName().trim());
		entity.setEnabled(request.isEnabled());
		entity.setEndpoint(normalize(request.getEndpoint()));
		entity.setSenderId(normalize(request.getSenderId()));
		entity.setChannelRegistrationId(normalize(request.getChannelRegistrationId()));
		if (request.getConnectionString() != null && !request.getConnectionString().isBlank()) {
			String salt = generateSalt();
			entity.setConnectionStringSalt(salt);
			entity.setConnectionStringHash(hashWithSalt(request.getConnectionString().trim(), salt));
			entity.setConnectionStringEncrypted(dataEncryptionService.encrypt(request.getConnectionString().trim()));
		}
		return toResponse(repository.save(entity));
	}

	@Override
	@Transactional(readOnly = true)
	public List<CommunicationConfigResponseDTO> findAll() {
		return repository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
	}

	@Override
	@Transactional(readOnly = true)
	public List<CommunicationConfigResponseDTO> findEnabled() {
		return repository.findAllByEnabledTrueOrderByServiceTypeAsc().stream().map(this::toResponse).collect(Collectors.toList());
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<CommunicationConfigEntity> findEnabledConfig(String serviceType) {
		if (serviceType == null || serviceType.isBlank()) {
			return Optional.empty();
		}
		return repository.findFirstByServiceTypeIgnoreCase(serviceType.trim())
				.filter(CommunicationConfigEntity::isEnabled);
	}

	private CommunicationConfigResponseDTO toResponse(CommunicationConfigEntity entity) {
		return new CommunicationConfigResponseDTO(entity.getId(), entity.getServiceType(), entity.isEnabled(), entity.getDisplayName(), entity.getEndpoint(), entity.getSenderId(), entity.getChannelRegistrationId(), entity.getCreatedAt(), entity.getUpdatedAt());
	}

	private String normalize(String value) {
		return value == null ? null : (value.trim().isEmpty() ? null : value.trim());
	}

	private String generateSalt() {
		byte[] salt = new byte[16];
		secureRandom.nextBytes(salt);
		return Base64.getEncoder().encodeToString(salt);
	}

	private String hashWithSalt(String value, String salt) {
		return dataEncryptionService.hashForLookup(salt + ":" + value);
	}
}
