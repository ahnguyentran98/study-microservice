package com.microservices.apigateway.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@Slf4j
public class FallbackController {

    @RequestMapping("/fallback")
    public Mono<ResponseEntity<Map<String, Object>>> fallback(ServerWebExchange exchange) {
        String path = exchange.getRequest().getPath().value();
        String method = exchange.getRequest().getMethod().name();
        
        log.warn("🔴 Circuit breaker activated for: {} {}", method, path);
        
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("status", HttpStatus.SERVICE_UNAVAILABLE.value());
        response.put("error", "Service Unavailable");
        response.put("message", "Service is temporarily unavailable. Please try again later.");
        response.put("path", path);
        response.put("method", method);
        
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response));
    }
    
    @RequestMapping("/fallback/user-service")
    public Mono<ResponseEntity<Map<String, Object>>> userServiceFallback(ServerWebExchange exchange) {
        String path = exchange.getRequest().getPath().value();
        
        log.warn("🔴 User Service circuit breaker activated for: {}", path);
        
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("status", HttpStatus.SERVICE_UNAVAILABLE.value());
        response.put("error", "User Service Unavailable");
        response.put("message", "User service is temporarily unavailable. Please try again later.");
        response.put("path", path);
        response.put("service", "user-service");
        
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response));
    }
    
    @RequestMapping("/fallback/product-service")
    public Mono<ResponseEntity<Map<String, Object>>> productServiceFallback(ServerWebExchange exchange) {
        String path = exchange.getRequest().getPath().value();
        
        log.warn("🔴 Product Service circuit breaker activated for: {}", path);
        
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("status", HttpStatus.SERVICE_UNAVAILABLE.value());
        response.put("error", "Product Service Unavailable");
        response.put("message", "Product service is temporarily unavailable. Please try again later.");
        response.put("path", path);
        response.put("service", "product-service");
        
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response));
    }
    
    @RequestMapping("/fallback/order-service")
    public Mono<ResponseEntity<Map<String, Object>>> orderServiceFallback(ServerWebExchange exchange) {
        String path = exchange.getRequest().getPath().value();
        
        log.warn("🔴 Order Service circuit breaker activated for: {}", path);
        
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("status", HttpStatus.SERVICE_UNAVAILABLE.value());
        response.put("error", "Order Service Unavailable");
        response.put("message", "Order service is temporarily unavailable. Please try again later.");
        response.put("path", path);
        response.put("service", "order-service");
        
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response));
    }
    
    @RequestMapping("/fallback/payment-service")
    public Mono<ResponseEntity<Map<String, Object>>> paymentServiceFallback(ServerWebExchange exchange) {
        String path = exchange.getRequest().getPath().value();
        
        log.warn("🔴 Payment Service circuit breaker activated for: {}", path);
        
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("status", HttpStatus.SERVICE_UNAVAILABLE.value());
        response.put("error", "Payment Service Unavailable");
        response.put("message", "Payment service is temporarily unavailable. Please try again later.");
        response.put("path", path);
        response.put("service", "payment-service");
        
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response));
    }
    
    @RequestMapping("/fallback/notification-service")
    public Mono<ResponseEntity<Map<String, Object>>> notificationServiceFallback(ServerWebExchange exchange) {
        String path = exchange.getRequest().getPath().value();
        
        log.warn("🔴 Notification Service circuit breaker activated for: {}", path);
        
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("status", HttpStatus.SERVICE_UNAVAILABLE.value());
        response.put("error", "Notification Service Unavailable");
        response.put("message", "Notification service is temporarily unavailable. Please try again later.");
        response.put("path", path);
        response.put("service", "notification-service");
        
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response));
    }
}