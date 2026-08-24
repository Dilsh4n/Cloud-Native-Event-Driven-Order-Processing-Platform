package com.orderplatform.inventory_service.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductResponse(
        UUID id,
        String sku,
        String name,
        BigDecimal price,
        int availableQuantity,
        int totalQuantity
) {
}
