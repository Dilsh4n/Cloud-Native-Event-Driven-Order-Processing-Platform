package com.orderplatform.order_service.command.eventStore;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OrderEventRepository extends JpaRepository<OrderEventEntity, UUID> {

    List<OrderEventEntity> findByOrderIdOrderBySequenceNumberAsc(UUID orderId);

}
