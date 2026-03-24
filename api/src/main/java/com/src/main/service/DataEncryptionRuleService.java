package com.src.main.service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.src.main.model.DataEncryptionRuleEntity;
import com.src.main.repository.DataEncryptionRuleRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DataEncryptionRuleService {

	private static final Duration CACHE_TTL = Duration.ofSeconds(30);

	private final DataEncryptionRuleRepository repository;
	private final ConcurrentMap<String, CacheEntry> cache = new ConcurrentHashMap<>();

	public boolean shouldEncrypt(String tableName, String columnName) {
		return getRule(tableName, columnName).encrypt();
	}

	public String getHashShadowColumn(String tableName, String columnName) {
		return getRule(tableName, columnName).hashShadowColumn();
	}

	@Transactional(readOnly = true)
	public List<DataEncryptionRuleEntity> listAll() {
		return repository.findAllByOrderByTableNameAscColumnNameAsc();
	}

	@Transactional
	public DataEncryptionRuleEntity save(DataEncryptionRuleEntity entity) {
		entity.setTableName(normalize(entity.getTableName()));
		entity.setColumnName(normalizeOptional(entity.getColumnName()));
		entity.setHashShadowColumn(normalizeOptional(entity.getHashShadowColumn()));
		DataEncryptionRuleEntity saved = repository.save(entity);
		evictCache(saved.getTableName());
		return saved;
	}

	@Transactional
	public void delete(java.util.UUID id) {
		DataEncryptionRuleEntity existing = repository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Data encryption rule not found: " + id));
		repository.delete(existing);
		evictCache(existing.getTableName());
	}

	private ResolvedRule getRule(String tableName, String columnName) {
		String normalizedTable = normalize(tableName);
		String normalizedColumn = normalize(columnName);
		if (normalizedTable.isBlank() || normalizedColumn.isBlank()) {
			return ResolvedRule.NONE;
		}
		TableRuleSet tableRuleSet = getTableRules(normalizedTable);
		if (tableRuleSet.hashShadowColumns().contains(normalizedColumn)) {
			return ResolvedRule.NONE;
		}
		ResolvedRule explicitRule = tableRuleSet.rulesByColumn().get(normalizedColumn);
		if (explicitRule != null) {
			return explicitRule;
		}
		return tableRuleSet.allColumnsRule() == null ? ResolvedRule.NONE : tableRuleSet.allColumnsRule();
	}

	private TableRuleSet getTableRules(String normalizedTable) {
		Instant now = Instant.now();
		CacheEntry current = cache.get(normalizedTable);
		if (current != null && current.expiresAt().isAfter(now)) {
			return current.ruleSet();
		}
		List<DataEncryptionRuleEntity> rules = repository.findByEnabledTrueAndTableNameIgnoreCase(normalizedTable);
		Map<String, ResolvedRule> rulesByColumn = new java.util.LinkedHashMap<>();
		java.util.Set<String> hashShadowColumns = new java.util.LinkedHashSet<>();
		ResolvedRule allColumnsRule = null;
		for (DataEncryptionRuleEntity rule : rules) {
			String normalizedColumn = normalize(rule.getColumnName());
			String normalizedHashShadowColumn = normalize(rule.getHashShadowColumn());
			ResolvedRule resolvedRule = new ResolvedRule(true,
					normalizedHashShadowColumn.isBlank() ? null : normalizedHashShadowColumn);
			if (!normalizedHashShadowColumn.isBlank()) {
				hashShadowColumns.add(normalizedHashShadowColumn);
			}
			if (normalizedColumn.isBlank()) {
				allColumnsRule = resolvedRule;
				continue;
			}
			rulesByColumn.put(normalizedColumn, resolvedRule);
		}
		TableRuleSet ruleSet = new TableRuleSet(Map.copyOf(rulesByColumn), java.util.Set.copyOf(hashShadowColumns), allColumnsRule);
		cache.put(normalizedTable, new CacheEntry(ruleSet, now.plus(CACHE_TTL)));
		return ruleSet;
	}

	private String normalize(String value) {
		return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
	}

	private String normalizeOptional(String value) {
		String normalized = normalize(value);
		return normalized.isBlank() ? null : normalized;
	}

	private void evictCache(String tableName) {
		if (tableName == null || tableName.isBlank()) {
			cache.clear();
			return;
		}
		cache.remove(normalize(tableName));
	}

	private record CacheEntry(TableRuleSet ruleSet, Instant expiresAt) {
	}

	private record TableRuleSet(Map<String, ResolvedRule> rulesByColumn, java.util.Set<String> hashShadowColumns, ResolvedRule allColumnsRule) {
	}

	private record ResolvedRule(boolean encrypt, String hashShadowColumn) {
		private static final ResolvedRule NONE = new ResolvedRule(false, null);
	}
}
