package com.orderplatform.inventory_service.controller;

import com.orderplatform.inventory_service.Exceptions.ProductNotFoundExceptions;
import com.orderplatform.inventory_service.dto.CreateProductRequest;
import com.orderplatform.inventory_service.dto.ProductResponse;
import com.orderplatform.inventory_service.entity.Product;
import com.orderplatform.inventory_service.repo.ProductRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Slf4j
public class ProductController {

    private final ProductRepository productRepository;

    @PostMapping
    public ResponseEntity<ProductResponse> create (@Valid @RequestBody CreateProductRequest request) {
        log.info("create request recieved");
        Product product = new Product();
        product.setSku(request.sku());
        product.setName(request.name());
        product.setPrice(request.price());
        product.setTotalQuantity(request.totalQuantity());
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(productRepository.save(product)));
    }

    @GetMapping
    public List<ProductResponse> list() {
        return productRepository.findAll().stream().map(this::toResponse).toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> get(@PathVariable UUID id) {
        Product product = productRepository.findById(id).orElseThrow(() -> new ProductNotFoundExceptions(id));
        return ResponseEntity.ok(toResponse(product));
    }

    private ProductResponse toResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getSku(),
                product.getName(),
                product.getPrice(),
                product.getAvailableQuantity(),
                product.getTotalQuantity()
        );
    }
}
