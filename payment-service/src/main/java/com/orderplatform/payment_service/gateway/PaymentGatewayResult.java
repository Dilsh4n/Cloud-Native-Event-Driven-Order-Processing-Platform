package com.orderplatform.payment_service.gateway;

public record PaymentGatewayResult (
        boolean approved,
        String transactionRef,
        String message
){
    public static PaymentGatewayResult approved(String txnRef){
        return new PaymentGatewayResult(true,  txnRef, "Approved");
    }

    public static PaymentGatewayResult declined(String message){
        return new PaymentGatewayResult(false, null, message);
    }
}
