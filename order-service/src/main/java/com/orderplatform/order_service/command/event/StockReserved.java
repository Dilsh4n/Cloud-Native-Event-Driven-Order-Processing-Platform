package com.orderplatform.order_service.command.event;

import java.util.UUID;

public record StockReserved(
        UUID orderId
) implements OrderEvent{ }
