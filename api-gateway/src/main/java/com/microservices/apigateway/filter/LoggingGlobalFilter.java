package com.microservices.apigateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
@Slf4j
public class LoggingGlobalFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String requestId = UUID.randomUUID().toString();
        
        // Log request details
        log.info("🔵 [REQUEST] {} | {} {} | Headers: {} | Remote Address: {} | Request ID: {}", 
                LocalDateTime.now(),
                request.getMethod(),
                request.getURI(),
                request.getHeaders().entrySet().stream()
                    .filter(entry -> !entry.getKey().toLowerCase().contains("authorization"))
                    .map(entry -> entry.getKey() + "=" + entry.getValue())
                    .toList(),
                request.getRemoteAddress(),
                requestId);
        
        // Add request ID to headers for downstream services
        ServerHttpRequest mutatedRequest = request.mutate()
                .header("X-Request-ID", requestId)
                .build();
        
        ServerWebExchange mutatedExchange = exchange.mutate()
                .request(mutatedRequest)
                .build();
        
        long startTime = System.currentTimeMillis();
        
        return chain.filter(mutatedExchange)
                .doOnSuccess(aVoid -> {
                    long duration = System.currentTimeMillis() - startTime;
                    log.info("🟢 [RESPONSE] {} | {} {} | Status: {} | Duration: {}ms | Request ID: {}", 
                            LocalDateTime.now(),
                            request.getMethod(),
                            request.getURI(),
                            exchange.getResponse().getStatusCode(),
                            duration,
                            requestId);
                })
                .doOnError(throwable -> {
                    long duration = System.currentTimeMillis() - startTime;
                    log.error("🔴 [ERROR] {} | {} {} | Error: {} | Duration: {}ms | Request ID: {}", 
                            LocalDateTime.now(),
                            request.getMethod(),
                            request.getURI(),
                            throwable.getMessage(),
                            duration,
                            requestId);
                });
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}