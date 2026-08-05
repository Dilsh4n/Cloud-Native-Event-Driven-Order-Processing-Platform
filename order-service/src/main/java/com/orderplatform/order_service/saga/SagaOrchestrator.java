package com.orderplatform.order_service.saga;

import com.orderplatform.order_service.command.domain.Order;
import com.orderplatform.order_service.command.event.*;
import com.orderplatform.order_service.command.eventStore.EventStore;
import com.orderplatform.order_service.idempotency.Idempotent;
import com.orderplatform.order_service.outbox.OutboxService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SagaOrchestrator {

    private final EventStore eventStore;
    private final OutboxService outboxService;

    @Idempotent(key = "#eventId")
    @Transactional
    public void onStockReserved(UUID eventId, UUID orderId) {

        Order order = eventStore.load(orderId);
        eventStore.append(orderId, order.getVersion(), new StockReserved(orderId));

        outboxService.enqueue("payment.commands", orderId, "ChargePayment", Map.of(
                "orderId", orderId.toString(), "amount", order.getTotalAmount()
        ));
        log.info("Order {}: stock reserved, ChargePayment enqueued", orderId);
    }

    @Idempotent(key = "#eventId")
    @Transactional
    public void onStockReservationFailed(UUID eventId, UUID orderId, String reason) {

        Order order = eventStore.load(orderId);
        int v = order.getVersion();
        eventStore.append(orderId, v, new StockReservationFailed(orderId, reason));
        eventStore.append(orderId, v + 1, new OrderCancelled(orderId, "Stock reservation failed: " + reason));
        log.info("Order {}: stock reservation failed ({}), order cancelled", orderId, reason);
    }

    @Idempotent(key = "#eventId")
    @Transactional
    public void onPaymentCompleted(UUID eventId, UUID orderId, String transactionRef) {

        Order order = eventStore.load(orderId);
        int v = order.getVersion();
        eventStore.append(orderId, v, new PaymentCompleted(orderId, transactionRef));
        eventStore.append(orderId, v + 1, new OrderConfirmed(orderId));
        log.info("Order {}: payment completed, order confirmed", orderId);
    }

    @Idempotent(key = "#eventId")
    @Transactional
    public void onPaymentFailed(UUID eventId, UUID orderId, String reason) {

        Order order = eventStore.load(orderId);
        int v = order.getVersion();
        eventStore.append(orderId, v, new PaymentFailed(orderId, reason));
        eventStore.append(orderId, v + 1, new OrderCancelled(orderId, "Payment failed: " + reason));

        outboxService.enqueue("inventory.commands", orderId, "ReleaseStock", Map.of("orderId", orderId.toString()));
        log.info("Order {}: payment failed ({}), stock release compensation enqueued", orderId, reason);
    }
}
