package com.orderplatform.order_service.query.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderDetailResponse (UUID orderId, UUID customerId, String status, BigDecimal totalAmount){
}
