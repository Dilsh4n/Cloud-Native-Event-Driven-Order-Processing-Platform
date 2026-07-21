package com.orderplatform.inventory_service.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String sku;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(nullable = false)
    private int totalQuantity;

    @Column(nullable = false)
    private int reservedQuantity = 0;

    @Version
    private Long version;

    public int getAvailableQuantity(){
        return totalQuantity - reservedQuantity;
    }

    public void reserve(int quantity){
        this.reservedQuantity += quantity;
    }

    public void release(int quantity){
        this.reservedQuantity = Math.max(0, this.reservedQuantity - quantity);
    }
}
