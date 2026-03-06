package com.example.order_service.service.client;

import java.math.BigDecimal;
import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class PaymentClient {

    private final WebClient webClient;

    public PaymentClient(WebClient.Builder loadBalancedBuilder) {
        this.webClient = loadBalancedBuilder.baseUrl("http://payment-service").build();
    }

    public void processPayment(Long orderId,
                               String paymentMethod,
                               String paymentToken,
                               BigDecimal amount,
                               String correlationId) {
        webClient.post()
                .uri("/api/v1/payments/process")
                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                .header("X-Correlation-ID", correlationId)
                .bodyValue(Map.of(
                        "orderId", orderId,
                        "paymentMethod", paymentMethod,
                        "paymentToken", paymentToken,
                        "amount", amount
                ))
                .retrieve()
                .bodyToMono(Void.class)
                .block();
    }

    public void refundPayment(Long orderId, String correlationId) {
        webClient.post()
                .uri("/api/v1/payments/refund")
                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                .header("X-Correlation-ID", correlationId)
                .bodyValue(Map.of("orderId", orderId))
                .retrieve()
                .bodyToMono(Void.class)
                .onErrorResume(ex -> Mono.empty()) // idempotent refund: ignore repeat failures
                .block();
    }
}
