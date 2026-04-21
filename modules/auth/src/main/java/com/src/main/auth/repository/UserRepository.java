package com.src.main.auth.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.src.main.auth.model.User;
import com.src.main.auth.model.UserStatus;

public interface UserRepository extends JpaRepository<User, String> {
	Optional<User> findByIdentifier(String identifier);

	@Query("""
			select u
			from User u
			left join u.profile p
			where u.status = :status
			  and (
				lower(coalesce(p.firstName, '')) like lower(concat('%', :query, '%'))
				or lower(coalesce(p.lastName, '')) like lower(concat('%', :query, '%'))
				or lower(trim(concat(coalesce(p.firstName, ''), ' ', coalesce(p.lastName, '')))) like lower(concat('%', :query, '%'))
			  )
			""")
	List<User> searchActiveUsersByProfile(@Param("query") String query, @Param("status") UserStatus status, Pageable pageable);
}
