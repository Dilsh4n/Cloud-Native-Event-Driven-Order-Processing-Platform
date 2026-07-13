package com.orderplatform.api_gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class RequestLoggingGlobalFilter implements GlobalFilter, Ordered {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
         ServerHttpRequest request = exchange.getRequest();
         String correlationId = exchange.getAttributes().get("correlationId").toString();
         long startTime = System.currentTimeMillis();

         log.info("[{}] --> {} {}", correlationId, request.getMethod(), request.getURI().getPath());

         return chain.filter(exchange).then(Mono.fromRunnable(() -> {
             long duration = System.currentTimeMillis() - startTime;
             HttpStatusCode status = exchange.getResponse().getStatusCode();
             log.info("[{}] <-- {} {} ({} ms)", correlationId, status, request.getURI().getPath(), duration);
         }));

    }

    @Override
    public int getOrder() {
        return -1;
    }
}
