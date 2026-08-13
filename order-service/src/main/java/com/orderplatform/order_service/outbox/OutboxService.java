package com.orderplatform.order_service.outbox;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.propagation.Propagator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OutboxService {

    private final OutboxMessageRepository outboxMessageRepository;
    private final ObjectMapper objectMapper;
    private final Tracer tracer;
    private final Propagator propagator;

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
            message.setTraceparent(captureTraceparent());
            outboxMessageRepository.save(message);
        } catch (JacksonException e) {
            throw new IllegalStateException("Failed to build outbox message", e);
        }
    }

    private String captureTraceparent() {
        Span currentSpan = tracer.currentSpan();
        if (currentSpan == null) return null;
        Map<String, String> carrier = new HashMap<>();
        propagator.inject(currentSpan.context(), carrier, Map::put);
        return carrier.get("traceparent");
    }
}
