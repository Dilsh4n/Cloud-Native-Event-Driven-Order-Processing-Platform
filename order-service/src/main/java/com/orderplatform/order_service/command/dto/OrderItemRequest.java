package com.orderplatform.order_service.command.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderItemRequest(
        @NotNull UUID productId,
        @Min(1) int quantity,
        @NotNull @Positive BigDecimal unitPrice) {
}
