package com.src.main.status;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;

import com.src.main.model.ProjectEntity;
import com.src.main.utils.ProjectStatus;

import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;

public interface ProjectRepository extends JpaRepository<ProjectEntity, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(@QueryHint(name = "jakarta.persistence.lock.timeout", value = "0"))
    @Query("SELECT p FROM ProjectEntity p WHERE p.status = :status ORDER BY p.createdAt ASC")
    List<ProjectEntity> findAllByStatusOrderByCreatedAtAsc(
            @Param("status") ProjectStatus status,
            Pageable pageable
    );

}
