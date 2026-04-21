package com.src.main.cdn.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.src.main.cdn.model.CdnImageUploadDraftEntity;

public interface CdnImageUploadDraftRepository extends JpaRepository<CdnImageUploadDraftEntity, UUID> {

	@Query(value = """
			select *
			from cdn_image_upload_draft
			where status = 'PENDING'
			order by queued_at asc nulls last, created_at asc
			limit 1
			for update skip locked
			""", nativeQuery = true)
	Optional<CdnImageUploadDraftEntity> lockNextPendingDraft();

	List<CdnImageUploadDraftEntity> findByIdIn(Collection<UUID> ids);

	List<CdnImageUploadDraftEntity> findTop100ByOrderByCreatedAtDesc();
}
