package com.src.main.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.src.main.model.AiLabsUsageEntity;

public interface AiLabsUsageRepository extends JpaRepository<AiLabsUsageEntity, String> {

	@Modifying
	@Query(value = """
			INSERT INTO ai_labs_usage (owner_user_id, usage_count, created_at, updated_at)
			VALUES (:ownerUserId, 0, now(), now())
			ON CONFLICT (owner_user_id) DO NOTHING
			""", nativeQuery = true)
	void insertIfAbsent(@Param("ownerUserId") String ownerUserId);

	@Modifying
	@Query(value = """
			UPDATE ai_labs_usage
			SET usage_count = usage_count + 1,
			    updated_at = now()
			WHERE owner_user_id = :ownerUserId
			""", nativeQuery = true)
	int incrementUsage(@Param("ownerUserId") String ownerUserId);

	@Modifying
	@Query(value = """
			UPDATE ai_labs_usage
			SET usage_count = usage_count + 1,
			    updated_at = now()
			WHERE owner_user_id = :ownerUserId
			  AND usage_count < :limit
			""", nativeQuery = true)
	int incrementUsageIfBelowLimit(@Param("ownerUserId") String ownerUserId, @Param("limit") int limit);
}
