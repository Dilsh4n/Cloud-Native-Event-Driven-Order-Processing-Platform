package com.orderplatform.order_service.query.projection;

import com.orderplatform.order_service.idempotency.Idempotent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderViewProjector{

    private final OrderViewRepository orderViewRepository;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "order.events", groupId = "order-service-query")
    @Transactional
    public void handle(String message) throws JacksonException {
        JsonNode node = objectMapper.readTree(message);
        UUID eventId = UUID.fromString(node.get("eventId").asString());
        String eventType = node.get("eventType").asString();
        UUID orderId = UUID.fromString(node.get("orderId").asString());
        project(eventId, eventType, orderId, node.get("payload"));
    }

    @Idempotent(key = "#eventId")
    private void project(UUID eventId, String eventType, UUID orderId, JsonNode payload) {
        switch (eventType) {
            case "OrderCreated" -> applyOrderCreated(orderId, payload);
            case "StockReserved" -> updateStatus(orderId, "STOCK_RESERVED");
            case "StockReservationFailed", "OrderCancelled", "PaymentFailed" -> updateStatus(orderId, "CANCELLED");
            case "PaymentCompleted" -> updateStatus(orderId, "PAID");
            case "OrderConfirmed" -> updateStatus(orderId, "CONFIRMED");
            default -> log.debug("Ignoring event type: {}", eventType);
        }
    }

    private void applyOrderCreated(UUID orderId, JsonNode payload) {
        OrderView orderView = OrderView.builder()
                .orderId(orderId)
                .customerId(UUID.fromString(payload.get("customerId").asString()))
                .status("PENDING")
                .totalAmount(payload.get("totalAmount").asDecimal())
                .itemsJson(payload.get("items").toString())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        orderViewRepository.save(orderView);
    }

    private void updateStatus(UUID orderId, String status) {
        OrderView orderView = orderViewRepository.findById(orderId)
                .orElseThrow(() -> new IllegalStateException("Read model missing for order: " + orderId));
        orderView.setStatus(status);
        orderView.setUpdatedAt(Instant.now());
        orderViewRepository.save(orderView);
    }
}
