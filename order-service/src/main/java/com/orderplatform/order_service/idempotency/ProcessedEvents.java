package com.orderplatform.order_service.idempotency;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Persistable;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "processed_events")
@Getter
@NoArgsConstructor
public class ProcessedEvents implements Persistable<UUID> {
    @Id
    private UUID eventId;

    @Column(nullable = false) private
    Instant processedAt;

    @Transient
    private boolean isNew = true;

    public ProcessedEvents(UUID eventId, Instant processedAt) {
        this.eventId = eventId;
        this.processedAt = processedAt;
    }

    @Override
    public @Nullable UUID getId() {
        return eventId;
    }

    @Override
    public boolean isNew() {
        return this.isNew;
    }

    @PostPersist
    @PostLoad
    void markNotNew() {
        this.isNew = false;
    }
}
