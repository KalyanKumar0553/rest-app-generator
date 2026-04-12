package com.src.main.cdn.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.src.main.cdn.model.CdnImageAssetEntity;

public interface CdnImageAssetRepository extends JpaRepository<CdnImageAssetEntity, UUID> {

	Optional<CdnImageAssetEntity> findByDraftId(UUID draftId);
}
