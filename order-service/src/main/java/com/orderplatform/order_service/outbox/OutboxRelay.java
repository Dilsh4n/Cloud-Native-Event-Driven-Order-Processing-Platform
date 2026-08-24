package com.orderplatform.order_service.outbox;


import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.propagation.Propagator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxRelay {

    private final OutboxMessageRepository outboxMessageRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final Tracer tracer;
    private final Propagator propagator;

    @Scheduled(fixedDelay = 500)
    @Transactional
    public void publishPending() {
        outboxMessageRepository.findTop50ByPublishedAtIsNullOrderByCreatedAtAsc()
                .forEach(this::publishOne);

    }

    private void publishOne(OutboxMessage outboxMessage) {
        Span span = resumeSpan(outboxMessage.getTraceparent());
        try (Tracer.SpanInScope ws = tracer.withSpan(span)) {
            kafkaTemplate.send(outboxMessage.getTopic(), outboxMessage.getMessageKey(), outboxMessage.getPayload())
                    .get(5, TimeUnit.SECONDS);
            outboxMessage.setPublishedAt(Instant.now());
            outboxMessageRepository.save(outboxMessage);
        } catch (Exception e) {
            log.error("Failed to publish outbox outboxMessage {}, will retry next cycle", outboxMessage.getId(), e);
        } finally {
            span.end();
        }
    }

    private Span resumeSpan(String traceparent) {
        if (traceparent == null){
            return tracer.nextSpan().name("outbox-relay-publish").start();
        }
        Map<String, String> carrier = Map.of("traceparent", traceparent);
        return propagator.extract(carrier, Map::get).name("outbox-relay-publish").start();
    }
}
