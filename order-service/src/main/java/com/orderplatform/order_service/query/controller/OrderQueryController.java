package com.orderplatform.order_service.query.controller;


import com.orderplatform.order_service.exception.OrderNotFoundException;
import com.orderplatform.order_service.query.dto.OrderDetailResponse;
import com.orderplatform.order_service.query.projection.OrderView;
import com.orderplatform.order_service.query.projection.OrderViewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderQueryController {
    private final OrderViewRepository orderViewRepository;

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderDetailResponse> getOrder(@PathVariable UUID orderId) {
        OrderView view = orderViewRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
        return ResponseEntity.ok(toResponse(view));
    }

    @GetMapping
    public List<OrderDetailResponse> getOrdersForCustomer(@RequestParam UUID customerId) {
        return orderViewRepository.findByCustomerIdOrderByCreatedAtDesc(customerId).stream()
                .map(this::toResponse).toList();
    }

    private OrderDetailResponse toResponse(OrderView view) {
        return new OrderDetailResponse(view.getOrderId(), view.getCustomerId(), view.getStatus(), view.getTotalAmount());
    }
}
