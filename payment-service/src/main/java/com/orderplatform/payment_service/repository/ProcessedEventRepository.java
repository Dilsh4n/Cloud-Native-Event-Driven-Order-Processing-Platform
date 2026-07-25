package com.orderplatform.payment_service.repository;

import com.orderplatform.payment_service.entity.ProcessedEvents;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProcessedEventRepository extends JpaRepository<ProcessedEvents, UUID> {
}
