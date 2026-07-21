package com.orderplatform.inventory_service.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.orderplatform.inventory_service.messaging.ReleaseStockCommand;
import com.orderplatform.inventory_service.messaging.ReserveStockCommand;
import com.orderplatform.inventory_service.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class InventoryCommandListener {

    private final InventoryService inventoryService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "inventory.commands", groupId = "inventory-service")
    public void handle(String message) throws JsonProcessingException {
        JsonNode node = objectMapper.readTree(message);
        UUID eventId = UUID.fromString(node.get("eventId").asText());
        String eventType = node.get("eventType").asText();
        JsonNode payload = node.get("payload");
        log.info("Received message with eventType: {}", eventType);

        switch (eventType){
            case "ReserveStock" -> inventoryService.reserveStock(eventId,
                    objectMapper.treeToValue(payload, ReserveStockCommand.class));
            case "ReleaseStock" -> inventoryService.releaseStock(eventId,
                    objectMapper.treeToValue(payload, ReleaseStockCommand.class));
            default -> log.warn("Unknown event type: {}", eventType);
        }
    }

}
