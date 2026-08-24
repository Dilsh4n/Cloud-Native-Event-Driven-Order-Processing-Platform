package com.orderplatform.payment_service.messaging;

import java.math.BigDecimal;
import java.util.UUID;

public record ChargePaymentCommand(
        UUID orderId,
        BigDecimal amount
) {
}
