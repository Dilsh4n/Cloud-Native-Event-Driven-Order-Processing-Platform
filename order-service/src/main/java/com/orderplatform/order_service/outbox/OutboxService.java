package com.orderplatform.order_service.outbox;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OutboxService {

    private final OutboxMessageRepository outboxMessageRepository;
    private final ObjectMapper objectMapper;

    public void enqueue(String topic, UUID orderId, String eventType, Map<String, Object> payload) {
        try {
            Map<String, Object> envelope = new LinkedHashMap<>();
            envelope.put("eventId", UUID.randomUUID().toString());
            envelope.put("eventType", eventType);
            envelope.put("occurredAt", Instant.now().toString());
            envelope.put("orderId", orderId.toString());
            envelope.put("payload", payload);

            OutboxMessage message = new OutboxMessage();
            message.setTopic(topic);
            message.setMessageKey(orderId.toString());
            message.setPayload(objectMapper.writeValueAsString(envelope));
            message.setCreatedAt(Instant.now());
            outboxMessageRepository.save(message);
        } catch (JacksonException e) {
            throw new IllegalStateException("Failed to build outbox message", e);
        }
    }
}
