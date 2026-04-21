package com.src.main.service;

import java.util.List;
import java.util.Optional;

import com.src.main.dto.CommunicationConfigRequestDTO;
import com.src.main.dto.CommunicationConfigResponseDTO;
import com.src.main.model.CommunicationConfigEntity;

public interface CommunicationConfigService {
	CommunicationConfigResponseDTO saveOrUpdate(CommunicationConfigRequestDTO request);
	List<CommunicationConfigResponseDTO> findAll();
	List<CommunicationConfigResponseDTO> findEnabled();
	Optional<CommunicationConfigEntity> findEnabledConfig(String serviceType);
}
