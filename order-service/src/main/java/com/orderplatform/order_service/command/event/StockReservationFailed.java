package com.orderplatform.order_service.command.event;

import java.util.UUID;

public record StockReservationFailed(
        UUID orderId,
        String reason
) implements OrderEvent{ }
