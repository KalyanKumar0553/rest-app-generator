package com.src.main.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.src.main.model.ArtifactAppEntity;
import com.src.main.model.ArtifactAppVersionEntity;

public interface ArtifactAppVersionRepository extends JpaRepository<ArtifactAppVersionEntity, UUID> {
	List<ArtifactAppVersionEntity> findByAppOrderByCreatedAtDesc(ArtifactAppEntity app);

	Optional<ArtifactAppVersionEntity> findByAppAndVersionCodeIgnoreCase(ArtifactAppEntity app, String versionCode);
}
