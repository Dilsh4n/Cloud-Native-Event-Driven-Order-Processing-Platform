package com.orderplatform.order_service.exception;

import java.util.UUID;

public class OrderNotFoundException extends RuntimeException {
    public OrderNotFoundException(UUID orderID) {
        super("Order not found: " + orderID);
    }
}
