package com.src.main.payment.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.src.main.payment.entity.OrderEntity;
import com.src.main.payment.enums.OrderStatus;

public interface OrderRepository extends JpaRepository<OrderEntity, UUID> {
	Optional<OrderEntity> findByOrderReference(String orderReference);
	List<OrderEntity> findTop100ByStatusInOrderByCreatedAtAsc(List<OrderStatus> statuses);
}
