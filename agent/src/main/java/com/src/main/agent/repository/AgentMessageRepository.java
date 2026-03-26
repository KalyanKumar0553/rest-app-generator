package com.src.main.agent.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.src.main.agent.model.AgentMessageEntity;

@Repository
public interface AgentMessageRepository extends JpaRepository<AgentMessageEntity, UUID> {

	List<AgentMessageEntity> findBySessionIdOrderBySequenceNumberAsc(UUID sessionId);

	int countBySessionId(UUID sessionId);

	void deleteBySessionId(UUID sessionId);
}
