package com.src.main.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.src.main.model.ArtifactAppEntity;

public interface ArtifactAppRepository extends JpaRepository<ArtifactAppEntity, UUID> {
	Optional<ArtifactAppEntity> findByCodeIgnoreCase(String code);
}
