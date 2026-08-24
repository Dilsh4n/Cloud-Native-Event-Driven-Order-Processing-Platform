package com.orderplatform.inventory_service.repo;

import com.orderplatform.inventory_service.entity.ProcessedEvents;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProcessedEventRepository extends JpaRepository<ProcessedEvents, UUID> {
}
