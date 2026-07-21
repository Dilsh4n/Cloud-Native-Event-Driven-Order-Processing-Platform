package com.orderplatform.inventory_service.Exceptions;

import java.util.UUID;

public class ProductNotFoundExceptions extends RuntimeException {
    public ProductNotFoundExceptions(UUID productId) {
        super("Product not found: " + productId);
    }
}
