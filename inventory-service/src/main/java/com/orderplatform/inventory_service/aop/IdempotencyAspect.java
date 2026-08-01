package com.orderplatform.inventory_service.aop;


import com.orderplatform.inventory_service.entity.ProcessedEvents;
import com.orderplatform.inventory_service.repo.ProcessedEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Aspect
@Component
@Order(100)
@RequiredArgsConstructor
@Slf4j
public class IdempotencyAspect {

    private final ProcessedEventRepository processedEventRepository;
    private final ExpressionParser expressionParser = new SpelExpressionParser();

    @Around(value = "@annotation(idempotent)", argNames = "joinPoint, idempotent")
    public Object checkIdempotency(ProceedingJoinPoint joinPoint, Idempotent idempotent) throws Throwable {

        UUID eventId = resolveKey(joinPoint, idempotent.key());

        if (processedEventRepository.existsById(eventId)) {
            log.info("Event {} already processed, skipping {}", eventId, joinPoint.getSignature().getName());
            return null;
        }

        processedEventRepository.save(new ProcessedEvents(eventId, Instant.now()));
        return joinPoint.proceed();
    }

    private UUID resolveKey(ProceedingJoinPoint joinPoint, String keyExpression) {
        log.info("joint point: [{}], keyExpression: [{}]", joinPoint, keyExpression);
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();

        String[] paramNames = signature.getParameterNames();
        Object[] args = joinPoint.getArgs();

        StandardEvaluationContext context = new StandardEvaluationContext();
        for (int i = 0; i < paramNames.length; i++) {
            context.setVariable(paramNames[i], args[i]);
        }
        return expressionParser.parseExpression(keyExpression).getValue(context, UUID.class);
    }
}
