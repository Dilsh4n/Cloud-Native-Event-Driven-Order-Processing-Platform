package com.orderplatform.inventory_service.messaging;

import java.util.UUID;

public record ReserveStockCommand(
        UUID orderId, UUID productId, int quantity
) {
}
