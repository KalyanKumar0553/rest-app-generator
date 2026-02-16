package com.src.main.repository;

import java.time.OffsetDateTime;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.src.main.model.UniqueVisitEntity;

public interface UniqueVisitRepository extends JpaRepository<UniqueVisitEntity, Long> {

    @Modifying
    @Query(value = """
            INSERT INTO unique_visits (ip_hash, first_seen_at, last_seen_at, hit_count)
            VALUES (:ipHash, :now, :now, 1)
            ON CONFLICT (ip_hash)
            DO UPDATE SET
                last_seen_at = EXCLUDED.last_seen_at,
                hit_count = unique_visits.hit_count + 1
            """, nativeQuery = true)
    void upsertVisit(@Param("ipHash") String ipHash, @Param("now") OffsetDateTime now);
}
