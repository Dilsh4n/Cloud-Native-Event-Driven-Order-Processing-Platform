package com.orderplatform.order_service.command.event;

import java.util.UUID;

public record PaymentCompleted(
        UUID orderId,
        String transactionRef
) implements OrderEvent{ }
