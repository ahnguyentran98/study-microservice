package com.example.order_service.service.client;

import com.example.order_service.dto.OrderItemRequest;
import java.math.BigDecimal;
import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class ProductClient {

    private final WebClient webClient;

    public ProductClient(WebClient.Builder loadBalancedBuilder) {
        this.webClient = loadBalancedBuilder.baseUrl("http://product-service").build();
    }

    public BigDecimal reserveInventory(OrderItemRequest item, String correlationId) {
        // Expect downstream to respond with { "unitPrice": 12.34 }
        return webClient.post()
                .uri("/api/v1/products/{id}/inventory/reserve", item.getProductId())
                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                .header("X-Correlation-ID", correlationId)
                .bodyValue(Map.of("quantity", item.getQuantity()))
                .retrieve()
                .bodyToMono(ReserveInventoryResponse.class)
                .map(ReserveInventoryResponse::unitPrice)
                .block();
    }

    public void releaseInventory(OrderItemRequest item, String correlationId) {
        webClient.post()
                .uri("/api/v1/products/{id}/inventory/release", item.getProductId())
                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                .header("X-Correlation-ID", correlationId)
                .bodyValue(Map.of("quantity", item.getQuantity()))
                .retrieve()
                .bodyToMono(Void.class)
                .onErrorResume(ex -> Mono.empty()) // release is best-effort
                .block();
    }

    private record ReserveInventoryResponse(BigDecimal unitPrice) {}
}
