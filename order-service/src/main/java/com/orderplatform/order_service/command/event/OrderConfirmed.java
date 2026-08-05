package com.orderplatform.order_service.command.event;

import java.util.UUID;

public record OrderConfirmed (
        UUID orderId
) implements OrderEvent{ }
