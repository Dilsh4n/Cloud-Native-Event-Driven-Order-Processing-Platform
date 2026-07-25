package com.orderplatform.payment_service.dto;


import java.math.BigDecimal;
import java.util.UUID;

public record PaymentResponse (
        UUID id,
        UUID orderId,
        BigDecimal amount,
        String status,
        String transactionRef
){
}
