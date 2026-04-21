package com.src.main.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.src.main.model.CommunicationConfigEntity;

public interface CommunicationConfigRepository extends JpaRepository<CommunicationConfigEntity, UUID> {
	Optional<CommunicationConfigEntity> findFirstByServiceTypeIgnoreCase(String serviceType);
	List<CommunicationConfigEntity> findAllByEnabledTrueOrderByServiceTypeAsc();
}
