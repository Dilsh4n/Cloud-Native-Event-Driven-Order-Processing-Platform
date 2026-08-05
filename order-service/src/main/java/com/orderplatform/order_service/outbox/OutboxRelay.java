package com.orderplatform.order_service.outbox;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxRelay {

    private final OutboxMessageRepository outboxMessageRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Scheduled(fixedDelay = 500)
    @Transactional
    public void publishPending() {

        List<OutboxMessage> pending = outboxMessageRepository.findTop50ByPublishedAtIsNullOrderByCreatedAtAsc();
        for (OutboxMessage message: pending) {
            try {
                kafkaTemplate.send(message.getTopic(), message.getMessageKey(), message.getPayload());
                message.setPublishedAt(Instant.now());
                outboxMessageRepository.save(message);
            } catch (Exception e) {
                log.error("Failed to publish outbox message {}, will retry next cycle", message.getId(), e);
                //deliberately
            }
        }

    }
}
