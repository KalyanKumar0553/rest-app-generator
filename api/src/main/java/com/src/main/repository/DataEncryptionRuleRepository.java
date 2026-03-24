package com.src.main.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.src.main.model.DataEncryptionRuleEntity;

public interface DataEncryptionRuleRepository extends JpaRepository<DataEncryptionRuleEntity, java.util.UUID> {
	List<DataEncryptionRuleEntity> findByEnabledTrueAndTableNameIgnoreCase(String tableName);

	List<DataEncryptionRuleEntity> findAllByOrderByTableNameAscColumnNameAsc();
}
