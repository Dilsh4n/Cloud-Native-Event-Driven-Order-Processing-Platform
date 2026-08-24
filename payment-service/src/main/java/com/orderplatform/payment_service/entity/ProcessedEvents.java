package com.orderplatform.payment_service.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Persistable;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "processed_events")
@Getter
@Setter
@NoArgsConstructor
public class ProcessedEvents implements Persistable<UUID> {

    @Id
    private UUID id;

    @Column(nullable = false)
    private Instant processedAt;

    @Transient
    private boolean isNew = true;

    public ProcessedEvents(UUID id, Instant processedAt) {
        this.id = id;
        this.processedAt = processedAt;
    }

    @Override
    public @Nullable UUID getId() {
        return this.id;
    }

    @Override
    public boolean isNew() {
        return this.isNew;
    }

    @PostPersist
    @PostLoad
    void markNotNew(){
        this.isNew = false;
    }
}
