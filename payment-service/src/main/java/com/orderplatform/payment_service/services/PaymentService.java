package com.orderplatform.payment_service.services;

import com.orderplatform.payment_service.aop.Idempotent;
import com.orderplatform.payment_service.entity.Payment;
import com.orderplatform.payment_service.entity.PaymentStatus;
import com.orderplatform.payment_service.exceptions.PaymentNotFoundExceptions;
import com.orderplatform.payment_service.gateway.MockPaymentGateway;
import com.orderplatform.payment_service.gateway.PaymentGatewayResult;
import com.orderplatform.payment_service.messaging.ChargePaymentCommand;
import com.orderplatform.payment_service.messaging.RefundPaymentCommand;
import com.orderplatform.payment_service.repository.PaymentRepository;
import com.orderplatform.payment_service.repository.ProcessedEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final MockPaymentGateway mockPaymentGateway;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    private static final String EVENTS_TOPIC = "payment.events";

    @Idempotent(key = "#eventId")
    @Transactional
    public void chargePayment(UUID eventId, ChargePaymentCommand command){

        if (paymentRepository.existsByOrderId(command.orderId())){
            log.warn("Payment already exists for order {}, skipping duplicate charge", command.orderId());
            return;
        }

        PaymentGatewayResult result = mockPaymentGateway.charge(command.amount());

        Payment payment = new Payment();
        payment.setOrderId(command.orderId());
        payment.setAmount(command.amount());
        payment.setTransactionRef(result.transactionRef());

        if (result.approved()){
            payment.setStatus(PaymentStatus.COMPLETED);
            paymentRepository.save(payment);
            publishEvent("PaymentCompleted", command.orderId(), Map.of(
                    "orderId", command.orderId().toString(),
                    "amount", command.amount(),
                    "transactionRef", result.transactionRef()
            ));
        }else {
            payment.setStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);
            publishEvent("PaymentFailed", command.orderId(), Map.of(
                    "orderId", command.orderId().toString(),
                    "reason", result.message()
            ));
        }
    }


    @Idempotent(key = "#eventId")
    @Transactional
    public void refundPayment(UUID eventId, RefundPaymentCommand command){

        Payment payment = paymentRepository.findByOrderId(command.orderId()).orElseThrow(
                () -> new PaymentNotFoundExceptions(command.orderId())
        );

        if (payment.getStatus() != PaymentStatus.COMPLETED) {
            log.warn("Payment for order {} is not completed, cannot refund", command.orderId());
            return;
        }

        mockPaymentGateway.refund(payment.getTransactionRef());
        payment.setStatus(PaymentStatus.REFUNDED);
        paymentRepository.save(payment);
        publishEvent("PaymentRefunded", command.orderId(), Map.of(
                "orderId", command.orderId().toString()
        ));
    }

    private void publishEvent(String eventType, UUID orderId, Map<String, Object> payload) {
        try {
            Map<String, Object> envelop = new LinkedHashMap<>();
            envelop.put("eventId", UUID.randomUUID().toString());
            envelop.put("eventType", eventType);
            envelop.put("occurredAt", Instant.now().toString());
            envelop.put("orderId", orderId.toString());
            envelop.put("payload", payload);

            kafkaTemplate.send(EVENTS_TOPIC, orderId.toString(), objectMapper.writeValueAsString(envelop));
            log.info("Published event {} for order {}", eventType, orderId);
        }catch (JacksonException e){
            log.error("Failed to serialize event {} for order {}: {}", eventType, orderId, e.getMessage());
        }
    }
}
