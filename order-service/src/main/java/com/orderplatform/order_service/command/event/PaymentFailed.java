package com.orderplatform.order_service.command.event;

import java.util.UUID;

public record PaymentFailed (
        UUID orderId,
        String reason
) implements OrderEvent{ }
