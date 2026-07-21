package com.orderplatform.inventory_service.service;

import com.orderplatform.inventory_service.Exceptions.ProductNotFoundExceptions;
import com.orderplatform.inventory_service.entity.ProcessedEvents;
import com.orderplatform.inventory_service.entity.Product;
import com.orderplatform.inventory_service.entity.ReservationStatus;
import com.orderplatform.inventory_service.entity.StockReservation;
import com.orderplatform.inventory_service.messaging.ReleaseStockCommand;
import com.orderplatform.inventory_service.messaging.ReserveStockCommand;
import com.orderplatform.inventory_service.repo.ProcessedEventRepository;
import com.orderplatform.inventory_service.repo.ProductRepository;
import com.orderplatform.inventory_service.repo.StockReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryService {

    private final ProductRepository productRepository;
    private final StockReservationRepository stockReservationRepository;
    private final ProcessedEventRepository processedEventRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    private static final String EVENTS_TOPIC = "inventory.events";
    private static final Duration RESERVATION_TTL = Duration.ofMinutes(10);


    @Transactional
    public void reserveStock(UUID eventID, ReserveStockCommand command){
        if (processedEventRepository.existsById(eventID)) {
            log.info("Event {} has already been processed. Skipping.", eventID);
            return;
        }
        log.info("save processed event to the db");
        try {
            processedEventRepository.save(new ProcessedEvents(eventID, Instant.now()));
        } catch (Exception e) {
            log.warn("Failed to save processed event {}: {}", eventID, e.getMessage());
            throw new RuntimeException(e);
        }
        log.info("processed event save is complete");
        Product product = productRepository.findById(command.productId()).orElseThrow(
                () -> new ProductNotFoundExceptions(command.productId())
        );

        if (product.getAvailableQuantity() < command.quantity()) {
            log.info("Insufficient stock for {}: requested {}, available {}",
                    command.productId(), command.quantity(), product.getAvailableQuantity());

            publishEvent("StockReservationFailed", command.orderId(), Map.of(
                    "orderId", command.orderId().toString(),
                    "productId", command.productId().toString(),
                    "reason", "Insufficient stock"));

            return;
        }

        product.reserve(command.quantity());
        productRepository.save(product);

        StockReservation reservation = new StockReservation();
        reservation.setOrderId(command.orderId());
        reservation.setProductId(command.productId());
        reservation.setQuantity(command.quantity());
        reservation.setStatus(ReservationStatus.RESERVED);
        reservation.setExpiresAt(Instant.now().plus(RESERVATION_TTL));
        stockReservationRepository.save(reservation);

        publishEvent("StockReserved", command.orderId(), Map.of(
                "orderId", command.orderId().toString(),
                "productId", command.productId().toString(),
                "quantity", command.quantity()
        ));

    }

    @Transactional
    public void releaseStock(UUID eventID, ReleaseStockCommand command){
        if (processedEventRepository.existsById(eventID)) {
            log.info("Event {} has already been processed. Skipping.", eventID);
            return;
        }

        processedEventRepository.save(new ProcessedEvents(eventID, Instant.now()));

        List<StockReservation> reservation = stockReservationRepository.findByOrderIdAndStatus(
                command.orderId(),
                ReservationStatus.RESERVED);

        for (StockReservation res: reservation){
            Product product = productRepository.findById(res.getProductId())
                    .orElseThrow(() -> new ProductNotFoundExceptions(res.getProductId()));
            product.release(res.getQuantity());
            productRepository.save(product);
            res.setStatus(ReservationStatus.RELEASED);
            stockReservationRepository.save(res);
        }

        publishEvent("StockReleased", command.orderId(), Map.of("orderId", command.orderId().toString()));
    }

    private void publishEvent(String eventType, UUID orderID, Map<String, Object> payload) {
        try {
            Map<String, Object> envelope = new LinkedHashMap<>();
            envelope.put("eventId", UUID.randomUUID().toString());
            envelope.put("eventType", eventType);
            envelope.put("occurredAt", Instant.now().toString());
            envelope.put("orderId", orderID.toString());
            envelope.put("payload", payload);

            String json = objectMapper.writeValueAsString(envelope);
            kafkaTemplate.send(EVENTS_TOPIC, orderID.toString(), json);
            log.info("Published {} for order {}", eventType, orderID);
        } catch (JacksonException e) {
            log.error("Failed to serialize {} event for order {}", eventType, orderID, e);
        }
    }
}
