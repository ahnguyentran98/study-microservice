package com.example.order_service.repository;

import com.example.order_service.entity.Order;
import com.example.order_service.enums.OrderStatus;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByClientRequestId(String clientRequestId);
    boolean existsByIdAndStatusIn(Long id, Iterable<OrderStatus> statuses);
}
