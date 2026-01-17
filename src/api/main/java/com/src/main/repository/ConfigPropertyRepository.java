package com.src.main.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.src.main.model.ConfigProperty;

public interface ConfigPropertyRepository extends JpaRepository<ConfigProperty, Long> {

	Optional<ConfigProperty> findByPropertyKey(String propertyKey);

    List<ConfigProperty> findByCategoryOrderByPropertyKeyAsc(String category);
}
