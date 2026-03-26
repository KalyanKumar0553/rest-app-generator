package com.src.main.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import com.src.main.model.AiLabsJobHistoryEntity;

public interface AiLabsJobHistoryRepository extends JpaRepository<AiLabsJobHistoryEntity, UUID> {
	Optional<AiLabsJobHistoryEntity> findByIdAndOwnerUserId(UUID id, String ownerUserId);

	List<AiLabsJobHistoryEntity> findByOwnerUserIdOrderByCreatedAtDesc(String ownerUserId);
}
