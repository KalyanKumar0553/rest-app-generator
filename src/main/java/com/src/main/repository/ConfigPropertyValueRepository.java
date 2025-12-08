package com.src.main.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.src.main.model.ConfigPropertyValue;

public interface ConfigPropertyValueRepository extends JpaRepository<ConfigPropertyValue, Long> {
}
