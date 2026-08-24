package com.orderplatform.order_service.command.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record CreateOrderRequest (@NotNull UUID customerId, @NotEmpty List<OrderItemRequest> items){
}
