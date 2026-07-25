package com.orderplatform.payment_service.exceptions;

import java.util.UUID;

public class PaymentNotFoundExceptions extends RuntimeException {
    public PaymentNotFoundExceptions(UUID orderId) {
        super("No payment found for order: " + orderId);
    }
}
