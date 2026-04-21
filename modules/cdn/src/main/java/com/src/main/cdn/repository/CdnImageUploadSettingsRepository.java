package com.src.main.cdn.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.src.main.cdn.model.CdnImageUploadSettingsEntity;

public interface CdnImageUploadSettingsRepository extends JpaRepository<CdnImageUploadSettingsEntity, String> {
}
