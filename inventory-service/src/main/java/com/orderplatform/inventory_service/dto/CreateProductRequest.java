package com.orderplatform.inventory_service.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record CreateProductRequest(
        @NotBlank String sku,
        @NotBlank String name,
        @NotNull @Positive BigDecimal price,
        @Min(0) Integer totalQuantity) {
}
