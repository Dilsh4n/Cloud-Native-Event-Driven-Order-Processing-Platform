package com.orderplatform.payment_service.gateway;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Component
public class MockPaymentGateway {

    //to simulate a decline scenario
    private static final BigDecimal DECLINE_TRIGGER_AMOUNT = new BigDecimal("999.99");

    public PaymentGatewayResult charge(BigDecimal amount) {
        if (amount.compareTo(DECLINE_TRIGGER_AMOUNT) == 0) {
            return PaymentGatewayResult.declined("Card declined by issuer");
        }
        return PaymentGatewayResult.approved("txn_" + UUID.randomUUID());
    }

    public void refund(String transactionRef) {
        // simulated refund logic
    }
}
