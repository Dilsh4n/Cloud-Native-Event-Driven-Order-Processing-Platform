package com.orderplatform.payment_service.messaging;

import java.util.UUID;

public record RefundPaymentCommand(
        UUID orderId
) {
}
