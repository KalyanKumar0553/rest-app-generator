package com.src.main.agent.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.src.main.agent.model.AgentSessionEntity;
import com.src.main.agent.model.AgentSessionStatus;

@Repository
public interface AgentSessionRepository extends JpaRepository<AgentSessionEntity, UUID> {

	List<AgentSessionEntity> findByOwnerUserIdOrderByCreatedAtDesc(String ownerUserId);

	Optional<AgentSessionEntity> findByIdAndOwnerUserId(UUID id, String ownerUserId);

	List<AgentSessionEntity> findByOwnerUserIdAndStatusOrderByCreatedAtDesc(String ownerUserId, AgentSessionStatus status);

	long countByOwnerUserIdAndStatus(String ownerUserId, AgentSessionStatus status);
}
