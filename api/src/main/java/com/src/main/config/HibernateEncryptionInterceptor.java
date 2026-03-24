package com.src.main.config;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.hibernate.Interceptor;
import org.hibernate.type.Type;
import org.springframework.stereotype.Component;

import com.src.main.model.DataEncryptionRuleEntity;
import com.src.main.service.DataEncryptionRuleService;
import com.src.main.service.DataEncryptionService;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Component
public class HibernateEncryptionInterceptor implements Interceptor {

	private final DataEncryptionService dataEncryptionService;
	private final DataEncryptionRuleService dataEncryptionRuleService;
	private final ConcurrentMap<Class<?>, EntityEncryptionMetadata> metadataCache = new ConcurrentHashMap<>();

	public HibernateEncryptionInterceptor(
			DataEncryptionService dataEncryptionService,
			DataEncryptionRuleService dataEncryptionRuleService) {
		this.dataEncryptionService = dataEncryptionService;
		this.dataEncryptionRuleService = dataEncryptionRuleService;
	}

	@Override
	public boolean onLoad(Object entity, Object id, Object[] state, String[] propertyNames, Type[] types) {
		return transform(entity, state, propertyNames, false);
	}

	@Override
	public boolean onSave(Object entity, Object id, Object[] state, String[] propertyNames, Type[] types) {
		return transform(entity, state, propertyNames, true);
	}

	@Override
	public boolean onFlushDirty(
			Object entity,
			Object id,
			Object[] currentState,
			Object[] previousState,
			String[] propertyNames,
			Type[] types) {
		return transform(entity, currentState, propertyNames, true);
	}

	private boolean transform(Object entity, Object[] state, String[] propertyNames, boolean encrypt) {
		if (entity == null || state == null || propertyNames == null || entity instanceof DataEncryptionRuleEntity) {
			return false;
		}
		EntityEncryptionMetadata metadata = metadataCache.computeIfAbsent(entity.getClass(), this::buildMetadata);
		if (!metadata.supported()) {
			return false;
		}

		boolean changed = false;
		for (int i = 0; i < propertyNames.length; i++) {
			String columnName = metadata.propertyColumns().get(propertyNames[i]);
			if (columnName == null || !(state[i] instanceof String value)) {
				continue;
			}
			if (!dataEncryptionRuleService.shouldEncrypt(metadata.tableName(), columnName)) {
				continue;
			}
			String transformed = encrypt ? dataEncryptionService.encrypt(value) : dataEncryptionService.decrypt(value);
			if (!Objects.equals(value, transformed)) {
				state[i] = transformed;
				changed = true;
			}
			if (encrypt) {
				String hashShadowColumn = dataEncryptionRuleService.getHashShadowColumn(metadata.tableName(), columnName);
				if (hashShadowColumn != null && !hashShadowColumn.isBlank()) {
					String shadowProperty = metadata.columnProperties().get(hashShadowColumn);
					if (shadowProperty != null) {
						int shadowIndex = indexOfProperty(propertyNames, shadowProperty);
						if (shadowIndex >= 0) {
							String shadowHash = dataEncryptionService.hashForLookup(value);
							if (!Objects.equals(state[shadowIndex], shadowHash)) {
								state[shadowIndex] = shadowHash;
								changed = true;
							}
						}
					}
				}
			}
		}
		return changed;
	}

	private EntityEncryptionMetadata buildMetadata(Class<?> entityClass) {
		Table table = entityClass.getAnnotation(Table.class);
		if (table == null || table.name() == null || table.name().isBlank()) {
			return EntityEncryptionMetadata.unsupported();
		}
		Map<String, String> propertyColumns = new HashMap<>();
		Map<String, String> columnProperties = new HashMap<>();
		Class<?> current = entityClass;
		while (current != null && current != Object.class) {
			for (Field field : current.getDeclaredFields()) {
				if (field.getType() != String.class || field.isAnnotationPresent(Id.class)) {
					continue;
				}
				Column column = field.getAnnotation(Column.class);
				String columnName = column != null && column.name() != null && !column.name().isBlank()
						? column.name()
						: field.getName();
				String normalizedColumnName = normalize(columnName);
				propertyColumns.putIfAbsent(field.getName(), normalizedColumnName);
				columnProperties.putIfAbsent(normalizedColumnName, field.getName());
			}
			current = current.getSuperclass();
		}
		return new EntityEncryptionMetadata(normalize(table.name()), propertyColumns, columnProperties, true);
	}

	private String normalize(String value) {
		return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
	}

	private int indexOfProperty(String[] propertyNames, String propertyName) {
		for (int i = 0; i < propertyNames.length; i++) {
			if (Objects.equals(propertyNames[i], propertyName)) {
				return i;
			}
		}
		return -1;
	}

	private record EntityEncryptionMetadata(
			String tableName,
			Map<String, String> propertyColumns,
			Map<String, String> columnProperties,
			boolean supported) {
		private static EntityEncryptionMetadata unsupported() {
			return new EntityEncryptionMetadata("", Map.of(), Map.of(), false);
		}
	}
}
