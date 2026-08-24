package com.orderplatform.order_service.command.event;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderItem (
        UUID productId,
        int quantity,
        BigDecimal unitPrice
){ }
