package com.orderplatform.payment_service.kafka;

import com.orderplatform.payment_service.messaging.ChargePaymentCommand;
import com.orderplatform.payment_service.messaging.RefundPaymentCommand;
import com.orderplatform.payment_service.services.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.Objects;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentCommandListener {

    private final PaymentService paymentService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "payment.commands", groupId = "payment-service")
    public void handle(String message) throws JacksonException {

        UUID eventId = null;
        String eventType = null;
        JsonNode payload = null;
        try {
            JsonNode node = objectMapper.readTree(message);
            eventId = UUID.fromString(node.get("eventId").asString());
            eventType = node.get("eventType").asString();
            payload = node.get("payload");
        } catch (Exception e) {
            log.error("exception occurred: {}", e.getMessage() );
        }

        switch (Objects.requireNonNull(eventType)){
            case "ChargePayment" -> paymentService.chargePayment(eventId,
                    objectMapper.treeToValue(payload, ChargePaymentCommand.class));
            case "RefundPayment" -> paymentService.refundPayment(eventId,
                    objectMapper.treeToValue(payload, RefundPaymentCommand.class));
            default -> log.warn("Unknown event type: {}", eventType);
        }

    }
}
