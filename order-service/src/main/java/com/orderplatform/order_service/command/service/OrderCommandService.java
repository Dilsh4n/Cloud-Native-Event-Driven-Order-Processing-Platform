package com.orderplatform.order_service.command.service;

import com.orderplatform.order_service.command.dto.CreateOrderRequest;
import com.orderplatform.order_service.command.event.OrderCreated;
import com.orderplatform.order_service.command.event.OrderItem;
import com.orderplatform.order_service.command.eventStore.EventStore;
import com.orderplatform.order_service.outbox.OutboxService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderCommandService {

    private final EventStore eventStore;
    private final OutboxService outboxService;

    @Transactional
    public UUID createOrder(CreateOrderRequest request) {
        UUID orderId = UUID.randomUUID();
        List<OrderItem> items = request.items().stream()
                .map(i -> new OrderItem(i.productId(), i.quantity(), i.unitPrice()))
                .toList();
        BigDecimal total = items.stream()
                .map(i -> i.unitPrice().multiply(BigDecimal.valueOf(i.quantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        eventStore.append(orderId, 0, new OrderCreated(orderId, request.customerId(), items, total));

        OrderItem firstItem = items.getFirst();
        outboxService.enqueue("inventory.commands", orderId, "ReserveStock", Map.of(
                "orderId", orderId.toString(),
                "productId", firstItem.productId().toString(),
                "quantity", firstItem.quantity()
        ));
        log.info("Order {} created with total {}", orderId, total);
        return orderId;
    }
}
