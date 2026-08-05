package com.orderplatform.order_service.command.domain;

import com.orderplatform.order_service.command.event.*;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Getter
public class Order {
    private UUID orderId;
    private UUID customerId;
    private List<OrderItem> items;
    private BigDecimal totalAmount;
    private OrderStatus status;
    private int version;

    public static Order replay(List<OrderEvent> events) {
        Order order = new Order();
        events.forEach(order::apply);
        return order;
    }

    public void apply(OrderEvent event) {
        switch (event) {
            case OrderCreated e -> {
                this.orderId = e.orderId();
                this.customerId = e.customerId();
                this.items = e.items();
                this.totalAmount = e.totalAmount();
                this.status = OrderStatus.PENDING;
            }
            case StockReserved e -> this.status = OrderStatus.STOCK_RESERVED;
            case StockReservationFailed e -> this.status = OrderStatus.CANCELLED;
            case PaymentCompleted e -> this.status = OrderStatus.PAID;
            case PaymentFailed e -> this.status = OrderStatus.CANCELLED;
            case OrderConfirmed e -> this.status = OrderStatus.CONFIRMED;
            case OrderCancelled e -> this.status = OrderStatus.CANCELLED;
        }
        this.version++;
    }
}
