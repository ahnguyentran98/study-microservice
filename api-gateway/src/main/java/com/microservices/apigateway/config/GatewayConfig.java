package com.microservices.apigateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;

@Configuration
public class GatewayConfig {

    @Value("${USER_SERVICE_URL:http://localhost:8081}")
    private String userServiceUrl;

    @Value("${PRODUCT_SERVICE_URL:http://localhost:8082}")
    private String productServiceUrl;

    @Value("${ORDER_SERVICE_URL:http://localhost:8083}")
    private String orderServiceUrl;

    @Value("${PAYMENT_SERVICE_URL:http://localhost:8084}")
    private String paymentServiceUrl;

    @Value("${NOTIFICATION_SERVICE_URL:http://localhost:8085}")
    private String notificationServiceUrl;

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // User Service Routes
                .route("user-service-register", r -> r.path("/api/users/register")
                        .and().method(HttpMethod.POST)
                        .uri(userServiceUrl))
                .route("user-service-login", r -> r.path("/api/users/login")
                        .and().method(HttpMethod.POST)
                        .uri(userServiceUrl))
                .route("user-service-profile", r -> r.path("/api/users/profile")
                        .and().method(HttpMethod.GET, HttpMethod.PUT)
                        .uri(userServiceUrl))
                .route("user-service-admin", r -> r.path("/api/users/{id}")
                        .and().method(HttpMethod.GET, HttpMethod.DELETE)
                        .uri(userServiceUrl))
                
                // Product Service Routes
                .route("product-service-list", r -> r.path("/api/products")
                        .and().method(HttpMethod.GET, HttpMethod.POST)
                        .uri(productServiceUrl))
                .route("product-service-detail", r -> r.path("/api/products/{id}")
                        .and().method(HttpMethod.GET, HttpMethod.PUT, HttpMethod.DELETE)
                        .uri(productServiceUrl))
                .route("product-service-search", r -> r.path("/api/products/search")
                        .and().method(HttpMethod.POST)
                        .uri(productServiceUrl))
                .route("product-service-inventory", r -> r.path("/api/products/{id}/inventory")
                        .and().method(HttpMethod.PUT)
                        .uri(productServiceUrl))
                
                // Order Service Routes
                .route("order-service-create", r -> r.path("/api/orders")
                        .and().method(HttpMethod.POST)
                        .uri(orderServiceUrl))
                .route("order-service-user-orders", r -> r.path("/api/orders/user/{userId}")
                        .and().method(HttpMethod.GET)
                        .uri(orderServiceUrl))
                .route("order-service-detail", r -> r.path("/api/orders/{orderId}")
                        .and().method(HttpMethod.GET)
                        .uri(orderServiceUrl))
                .route("order-service-status", r -> r.path("/api/orders/{orderId}/status")
                        .and().method(HttpMethod.PUT)
                        .uri(orderServiceUrl))
                
                // Payment Service Routes
                .route("payment-service-process", r -> r.path("/api/payments/process")
                        .and().method(HttpMethod.POST)
                        .uri(paymentServiceUrl))
                .route("payment-service-refund", r -> r.path("/api/payments/refund")
                        .and().method(HttpMethod.POST)
                        .uri(paymentServiceUrl))
                .route("payment-service-order", r -> r.path("/api/payments/order/{orderId}")
                        .and().method(HttpMethod.GET)
                        .uri(paymentServiceUrl))
                
                // Notification Service Routes
                .route("notification-service-email", r -> r.path("/api/notifications/email")
                        .and().method(HttpMethod.POST)
                        .uri(notificationServiceUrl))
                .route("notification-service-sms", r -> r.path("/api/notifications/sms")
                        .and().method(HttpMethod.POST)
                        .uri(notificationServiceUrl))
                .route("notification-service-history", r -> r.path("/api/notifications/user/{userId}/history")
                        .and().method(HttpMethod.GET)
                        .uri(notificationServiceUrl))
                
                .build();
    }
}