package com.src.main.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.src.main.dto.DataEncryptionRuleRequestDTO;
import com.src.main.dto.DataEncryptionRuleResponseDTO;
import com.src.main.model.DataEncryptionRuleEntity;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DataEncryptionRuleAdminService {

	private final DataEncryptionRuleService dataEncryptionRuleService;

	@Transactional(readOnly = true)
	public List<DataEncryptionRuleResponseDTO> listRules() {
		return dataEncryptionRuleService.listAll().stream().map(this::toDto).toList();
	}

	@Transactional
	public DataEncryptionRuleResponseDTO createRule(DataEncryptionRuleRequestDTO request) {
		DataEncryptionRuleEntity entity = new DataEncryptionRuleEntity();
		apply(entity, request);
		return toDto(dataEncryptionRuleService.save(entity));
	}

	@Transactional
	public DataEncryptionRuleResponseDTO updateRule(UUID id, DataEncryptionRuleRequestDTO request) {
		DataEncryptionRuleEntity entity = dataEncryptionRuleService.listAll().stream()
				.filter(rule -> rule.getId().equals(id))
				.findFirst()
				.orElseThrow(() -> new IllegalArgumentException("Data encryption rule not found: " + id));
		apply(entity, request);
		return toDto(dataEncryptionRuleService.save(entity));
	}

	@Transactional
	public void deleteRule(UUID id) {
		dataEncryptionRuleService.delete(id);
	}

	private void apply(DataEncryptionRuleEntity entity, DataEncryptionRuleRequestDTO request) {
		entity.setTableName(request.getTableName());
		entity.setColumnName(request.getColumnName());
		entity.setHashShadowColumn(request.getHashShadowColumn());
		entity.setEnabled(request.isEnabled());
	}

	private DataEncryptionRuleResponseDTO toDto(DataEncryptionRuleEntity entity) {
		return new DataEncryptionRuleResponseDTO(
				entity.getId(),
				entity.getTableName(),
				entity.getColumnName(),
				entity.getHashShadowColumn(),
				entity.isEnabled(),
				entity.getCreatedAt(),
				entity.getUpdatedAt());
	}
}
