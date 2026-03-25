package com.src.main.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.src.main.model.PluginModuleEntity;

public interface PluginModuleRepository extends JpaRepository<PluginModuleEntity, UUID> {

	Optional<PluginModuleEntity> findByCodeIgnoreCase(String code);

	List<PluginModuleEntity> findAllByOrderByNameAsc();

	List<PluginModuleEntity> findByEnabledTrueOrderByNameAsc();
}
