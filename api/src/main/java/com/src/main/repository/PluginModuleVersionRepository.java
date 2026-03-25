package com.src.main.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.src.main.model.PluginModuleVersionEntity;

public interface PluginModuleVersionRepository extends JpaRepository<PluginModuleVersionEntity, UUID> {

	List<PluginModuleVersionEntity> findByPluginModuleIdOrderByCreatedAtDesc(UUID pluginModuleId);

	List<PluginModuleVersionEntity> findByPluginModuleIdInOrderByCreatedAtDesc(Collection<UUID> pluginModuleIds);

	Optional<PluginModuleVersionEntity> findByPluginModuleIdAndVersionCodeIgnoreCase(UUID pluginModuleId, String versionCode);
}
