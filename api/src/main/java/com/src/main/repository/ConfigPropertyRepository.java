package com.src.main.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

import com.src.main.model.ConfigProperty;

public interface ConfigPropertyRepository extends JpaRepository<ConfigProperty, Long> {

	@EntityGraph(attributePaths = "allowedValues")
	Optional<ConfigProperty> findByPropertyKey(String propertyKey);

	@EntityGraph(attributePaths = "allowedValues")
    List<ConfigProperty> findByCategoryOrderByPropertyKeyAsc(String category);
}
