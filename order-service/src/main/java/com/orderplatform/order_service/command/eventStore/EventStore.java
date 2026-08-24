package com.orderplatform.order_service.command.eventStore;


import com.orderplatform.order_service.command.domain.Order;
import com.orderplatform.order_service.command.event.*;
import com.orderplatform.order_service.exception.OrderNotFoundException;
import com.orderplatform.order_service.outbox.OutboxService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EventStore {

    private final OrderEventRepository orderEventRepository;
    private final ObjectMapper objectMapper;
    private final OutboxService outboxService;

    private static final Map<String, Class<? extends OrderEvent>> EVENT_TYPES = Map.of(
            "OrderCreated", OrderCreated.class,
            "StockReserved", StockReserved.class,
            "StockReservationFailed", StockReservationFailed.class,
            "PaymentCompleted", PaymentCompleted.class,
            "PaymentFailed", PaymentFailed.class,
            "OrderConfirmed", OrderConfirmed.class,
            "OrderCancelled", OrderCancelled.class
    );

    @Transactional
    public void append(UUID orderId, int expectedVersion, OrderEvent event) {
        OrderEventEntity entity = new OrderEventEntity();
        entity.setOrderId(orderId);
        entity.setSequenceNumber(expectedVersion + 1);
        entity.setEventType(event.getClass().getSimpleName());
        entity.setOccurredAt(Instant.now());
        try {
            entity.setEventData(objectMapper.writeValueAsString(event));
        } catch (JacksonException e) {
            throw new IllegalStateException("Failed to serialize event", e);
        }
        orderEventRepository.save(entity); // unique constraint is what actually enforces the concurrency guarantee
        outboxService.enqueue("order.events", orderId, event.getClass().getSimpleName(),
                objectMapper.convertValue(event, new TypeReference<>() {}));
    }

    public Order load(UUID orderId) {
        List<OrderEventEntity> entities = orderEventRepository.findByOrderIdOrderBySequenceNumberAsc(orderId);
        if (entities.isEmpty()) {
            throw new OrderNotFoundException(orderId);
        }
        return Order.replay(
                entities.stream()
                        .map(this::deserialize)
                        .toList()
        );
    }


    private OrderEvent deserialize(OrderEventEntity entity) {
        try {
            return objectMapper.readValue(entity.getEventData(), EVENT_TYPES.get(entity.getEventType()));
        } catch (JacksonException e) {
            throw new IllegalStateException("Failed to deserialize event " + entity.getId(), e);
        }
    }


}
