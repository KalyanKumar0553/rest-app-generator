package com.src.main.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.src.main.model.ProjectTabDefinitionEntity;

public interface ProjectTabDefinitionRepository extends JpaRepository<ProjectTabDefinitionEntity, UUID> {

	List<ProjectTabDefinitionEntity> findAllByOrderByGeneratorLanguageAscDisplayOrderAscTabKeyAsc();

	List<ProjectTabDefinitionEntity> findByGeneratorLanguageIgnoreCaseAndEnabledTrueOrderByDisplayOrderAscTabKeyAsc(String generatorLanguage);

	Optional<ProjectTabDefinitionEntity> findByGeneratorLanguageIgnoreCaseAndTabKeyIgnoreCase(String generatorLanguage, String tabKey);
}
