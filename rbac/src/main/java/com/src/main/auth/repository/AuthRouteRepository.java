package com.src.main.auth.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.src.main.auth.model.AuthRoute;
import com.src.main.auth.repository.query.AuthRouteQueries;
public interface AuthRouteRepository extends JpaRepository<AuthRoute, java.util.UUID> {
	@Query(AuthRouteQueries.FIND_ACTIVE_ROUTES)
	List<AuthRoute> findActiveRoutes();
}
