package com.orderplatform.order_service.idempotency;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProcessedEventRepository extends JpaRepository<ProcessedEvents, UUID> {
}
