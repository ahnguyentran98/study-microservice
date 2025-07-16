package com.microservices.apigateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;

@Configuration
public class GatewayConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // User Service Routes
                .route("user-service-register", r -> r.path("/api/users/register")
                        .and().method(HttpMethod.POST)
                        .uri("http://localhost:8081"))
                .route("user-service-login", r -> r.path("/api/users/login")
                        .and().method(HttpMethod.POST)
                        .uri("http://localhost:8081"))
                .route("user-service-profile", r -> r.path("/api/users/profile")
                        .and().method(HttpMethod.GET, HttpMethod.PUT)
                        .uri("http://localhost:8081"))
                .route("user-service-admin", r -> r.path("/api/users/{id}")
                        .and().method(HttpMethod.GET, HttpMethod.DELETE)
                        .uri("http://localhost:8081"))
                
                // Product Service Routes
                .route("product-service-list", r -> r.path("/api/products")
                        .and().method(HttpMethod.GET, HttpMethod.POST)
                        .uri("http://localhost:8082"))
                .route("product-service-detail", r -> r.path("/api/products/{id}")
                        .and().method(HttpMethod.GET, HttpMethod.PUT, HttpMethod.DELETE)
                        .uri("http://localhost:8082"))
                .route("product-service-search", r -> r.path("/api/products/search")
                        .and().method(HttpMethod.POST)
                        .uri("http://localhost:8082"))
                .route("product-service-inventory", r -> r.path("/api/products/{id}/inventory")
                        .and().method(HttpMethod.PUT)
                        .uri("http://localhost:8082"))
                
                // Order Service Routes
                .route("order-service-create", r -> r.path("/api/orders")
                        .and().method(HttpMethod.POST)
                        .uri("http://localhost:8083"))
                .route("order-service-user-orders", r -> r.path("/api/orders/user/{userId}")
                        .and().method(HttpMethod.GET)
                        .uri("http://localhost:8083"))
                .route("order-service-detail", r -> r.path("/api/orders/{orderId}")
                        .and().method(HttpMethod.GET)
                        .uri("http://localhost:8083"))
                .route("order-service-status", r -> r.path("/api/orders/{orderId}/status")
                        .and().method(HttpMethod.PUT)
                        .uri("http://localhost:8083"))
                
                // Payment Service Routes
                .route("payment-service-process", r -> r.path("/api/payments/process")
                        .and().method(HttpMethod.POST)
                        .uri("http://localhost:8084"))
                .route("payment-service-refund", r -> r.path("/api/payments/refund")
                        .and().method(HttpMethod.POST)
                        .uri("http://localhost:8084"))
                .route("payment-service-order", r -> r.path("/api/payments/order/{orderId}")
                        .and().method(HttpMethod.GET)
                        .uri("http://localhost:8084"))
                
                // Notification Service Routes
                .route("notification-service-email", r -> r.path("/api/notifications/email")
                        .and().method(HttpMethod.POST)
                        .uri("http://localhost:8085"))
                .route("notification-service-sms", r -> r.path("/api/notifications/sms")
                        .and().method(HttpMethod.POST)
                        .uri("http://localhost:8085"))
                .route("notification-service-history", r -> r.path("/api/notifications/user/{userId}/history")
                        .and().method(HttpMethod.GET)
                        .uri("http://localhost:8085"))
                
                .build();
    }
}