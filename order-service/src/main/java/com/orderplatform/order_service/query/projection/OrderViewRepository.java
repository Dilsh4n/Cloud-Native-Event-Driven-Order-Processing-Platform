package com.orderplatform.order_service.query.projection;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OrderViewRepository extends JpaRepository<OrderView, UUID> {

    List<OrderView> findByCustomerIdOrderByCreatedAtAsc(UUID customerId);

    List<OrderView> findByCustomerIdOrderByCreatedAtDesc(UUID customerId);
}
