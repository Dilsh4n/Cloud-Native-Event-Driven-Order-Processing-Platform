package com.orderplatform.order_service.saga;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class SagaEventListener {

    private final SagaOrchestrator sagaOrchestrator;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = {"inventory.events", "payment.events"}, groupId = "order-service")
    public void handle(String message) throws JacksonException {
        JsonNode node = objectMapper.readTree(message);
        UUID eventId = UUID.fromString(node.get("eventId").asString());
        String eventType = node.get("eventType").asString();
        UUID orderId = UUID.fromString(node.get("orderId").asString());
        JsonNode payload = node.get("payload");

        switch (eventType) {
            case "StockReserved" -> sagaOrchestrator.onStockReserved(eventId, orderId);
            case "StockReservationFailed" -> sagaOrchestrator.onStockReservationFailed(eventId, orderId, payload.get("reason").asString());
            case "PaymentCompleted" -> sagaOrchestrator.onPaymentCompleted(eventId, orderId, payload.get("transactionRef").asString());
            case "PaymentFailed" -> sagaOrchestrator.onPaymentFailed(eventId, orderId, payload.get("reason").asString());
            default -> log.debug("Ignoring event type: {}", eventType);
        }
    }
}
