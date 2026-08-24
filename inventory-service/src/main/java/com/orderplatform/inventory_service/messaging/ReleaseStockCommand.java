package com.orderplatform.inventory_service.messaging;

import java.util.UUID;

public record ReleaseStockCommand (
        UUID orderId
){
}
