package com.orderplatform.order_service.command.controller;

import com.orderplatform.order_service.command.dto.CreateOrderRequest;
import com.orderplatform.order_service.command.dto.CreateOrderResponse;
import com.orderplatform.order_service.command.eventStore.EventStore;
import com.orderplatform.order_service.command.service.OrderCommandService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/orders")
@Slf4j
public class OrderCommandController {

    private final OrderCommandService orderCommandService;
    private final EventStore eventStore;


    @PostMapping
    public ResponseEntity<CreateOrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        UUID orderId = orderCommandService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(new CreateOrderResponse(orderId));
    }
}
