package com.orderplatform.order_service.command.event;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record OrderCreated(
        UUID orderId,
        UUID customerId,
        List<OrderItem> items,
        BigDecimal totalAmount
) implements OrderEvent{ }
