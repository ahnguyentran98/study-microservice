package com.microservices.apigateway.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
public class HealthController {

    @GetMapping("/health")
    public Mono<ResponseEntity<Map<String, Object>>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("service", "api-gateway");
        response.put("version", "1.0.0");
        
        return Mono.just(ResponseEntity.ok(response));
    }
    
    @GetMapping("/info")
    public Mono<ResponseEntity<Map<String, Object>>> info() {
        Map<String, Object> response = new HashMap<>();
        response.put("app", "API Gateway");
        response.put("description", "Spring Cloud Gateway for Microservices");
        response.put("version", "1.0.0");
        response.put("timestamp", LocalDateTime.now().toString());
        
        Map<String, Object> routes = new HashMap<>();
        routes.put("user-service", "http://localhost:8081");
        routes.put("product-service", "http://localhost:8082");
        routes.put("order-service", "http://localhost:8083");
        routes.put("payment-service", "http://localhost:8084");
        routes.put("notification-service", "http://localhost:8085");
        
        response.put("routes", routes);
        
        return Mono.just(ResponseEntity.ok(response));
    }
}