package com.orderplatform.order_service.command.event;

import java.util.UUID;

public sealed interface OrderEvent  permits
        OrderCreated,
        StockReserved,
        StockReservationFailed,
        PaymentCompleted,
        PaymentFailed,
        OrderConfirmed,
        OrderCancelled {
    UUID orderId();
}
