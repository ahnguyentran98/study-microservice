package com.example.order_service.controller;

import com.example.order_service.dto.CancelOrderResponse;
import com.example.order_service.dto.CreateOrderRequest;
import com.example.order_service.dto.OrderResponse;
import com.example.order_service.service.SagaOrderService;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private static final String CORRELATION_HEADER = "X-Correlation-ID";
    private final SagaOrderService sagaOrderService;

    public OrderController(SagaOrderService sagaOrderService) {
        this.sagaOrderService = sagaOrderService;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(
            @Valid @RequestBody CreateOrderRequest request,
            @RequestHeader(value = CORRELATION_HEADER, required = false) String correlationId) {

        String corrId = correlationId != null ? correlationId : UUID.randomUUID().toString();
        OrderResponse response = sagaOrderService.createOrder(request, corrId);
        return ResponseEntity.status(201).header(CORRELATION_HEADER, corrId).body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<CancelOrderResponse> cancelOrder(
            @PathVariable Long id,
            @RequestHeader(value = CORRELATION_HEADER, required = false) String correlationId) {

        String corrId = correlationId != null ? correlationId : UUID.randomUUID().toString();
        CancelOrderResponse response = sagaOrderService.cancelOrder(id, corrId);
        return ResponseEntity.ok().header(CORRELATION_HEADER, corrId).body(response);
    }
}
