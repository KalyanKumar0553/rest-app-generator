package com.src.main.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import com.src.main.model.OtpAttempt;


public interface OtpAttemptRepository extends JpaRepository<OtpAttempt, Long> {

	Optional<OtpAttempt> findByUsernameAndCreatedAtBetweenOrderByCreatedAtDesc(String username,LocalDateTime startTime,LocalDateTime endTime);

	@Transactional
	@Modifying
	@Query(value = "DELETE FROM OtpAttempt o WHERE o.username = :username")
	void deleteAllByUsername(String username);

}
