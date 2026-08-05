package com.orderplatform.order_service.command.eventStore;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@NoArgsConstructor
@Table(name = "order_events", uniqueConstraints = @UniqueConstraint(columnNames = {"order_id", "sequence_number"}))
@Getter
@Setter
public class OrderEventEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "order_id", nullable = false)
    private UUID orderId;

    @Column(name = "sequence_number", nullable = false)
    private int sequenceNumber;

    @Column(name = "event_type", nullable = false)
    private String eventType;

    @Column(name = "event_data", nullable = false, columnDefinition = "TEXT")
    //Text is used to store large amounts of text data, such as JSON or XML, in a database column.
    // It allows for the storage of variable-length character data that can exceed the typical limits of standard string types.
    private String eventData;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

}
