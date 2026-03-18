package com.src.main.repository;

import java.time.OffsetDateTime;

import org.springframework.data.jpa.repository.JpaRepository;

import com.src.main.model.UniqueVisitEntity;

public interface UniqueVisitRepository extends JpaRepository<UniqueVisitEntity, Long> {
    java.util.Optional<UniqueVisitEntity> findByIpHash(String ipHash);
}
