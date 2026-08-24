package com.orderplatform.order_service.outbox;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OutboxMessageRepository extends JpaRepository<OutboxMessage, UUID> {
    List<OutboxMessage> findTop50ByPublishedAtIsNullOrderByCreatedAtAsc();
}
