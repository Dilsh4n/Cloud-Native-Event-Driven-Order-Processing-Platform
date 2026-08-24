package com.orderplatform.payment_service.controller;

import com.orderplatform.payment_service.dto.PaymentResponse;
import com.orderplatform.payment_service.entity.Payment;
import com.orderplatform.payment_service.exceptions.PaymentNotFoundExceptions;
import com.orderplatform.payment_service.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/payments")
public class PaymentController {
    private final PaymentRepository paymentRepository;

    @GetMapping("/order/{orderid}")
    public ResponseEntity<PaymentResponse> getByOrder(@PathVariable("orderid") UUID orderId){
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new PaymentNotFoundExceptions(orderId));

        return ResponseEntity.ok(new PaymentResponse(payment.getId(), payment.getOrderId(),
                payment.getAmount(), payment.getStatus().name(), payment.getTransactionRef()));

    }
}
