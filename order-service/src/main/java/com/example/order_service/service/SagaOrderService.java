package com.example.order_service.service;

import com.example.order_service.dto.CancelOrderResponse;
import com.example.order_service.dto.CreateOrderRequest;
import com.example.order_service.dto.OrderItemRequest;
import com.example.order_service.dto.OrderItemResponse;
import com.example.order_service.dto.OrderResponse;
import com.example.order_service.entity.Order;
import com.example.order_service.entity.OrderItem;
import com.example.order_service.enums.OrderStatus;
import com.example.order_service.exception.BusinessException;
import com.example.order_service.exception.ResourceNotFoundException;
import com.example.order_service.repository.OrderRepository;
import com.example.order_service.service.client.PaymentClient;
import com.example.order_service.service.client.ProductClient;
import com.example.order_service.service.event.OrderEventPublisher;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SagaOrderService {

    private static final Logger log = LoggerFactory.getLogger(SagaOrderService.class);

    private final OrderRepository orderRepository;
    private final ProductClient productClient;
    private final PaymentClient paymentClient;
    private final OrderEventPublisher eventPublisher;

    public SagaOrderService(OrderRepository orderRepository,
                            ProductClient productClient,
                            PaymentClient paymentClient,
                            OrderEventPublisher eventPublisher) {
        this.orderRepository = orderRepository;
        this.productClient = productClient;
        this.paymentClient = paymentClient;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request, String correlationId) {
        // Idempotency: return existing order if same clientRequestId is seen
        Order existing = orderRepository.findByClientRequestId(request.getClientRequestId()).orElse(null);
        if (existing != null) {
            log.info("Idempotent hit for clientRequestId={}, orderId={}", request.getClientRequestId(), existing.getId());
            return toResponse(existing, correlationId);
        }

        OffsetDateTime now = OffsetDateTime.now();
        Order order = new Order();
        order.setUserId(request.getUserId());
        order.setStatus(OrderStatus.PENDING);
        order.setTotalAmount(BigDecimal.ZERO);
        order.setClientRequestId(request.getClientRequestId());
        order.setCreatedAt(now);
        order.setUpdatedAt(now);
        order.setItems(new ArrayList<>());
        order = orderRepository.save(order);

        List<OrderItem> persistedItems = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        try {
            for (OrderItemRequest itemRequest : request.getItems()) {
                BigDecimal unitPrice = productClient.reserveInventory(itemRequest, correlationId);
                OrderItem item = new OrderItem();
                item.setProductId(itemRequest.getProductId());
                item.setQuantity(itemRequest.getQuantity());
                item.setUnitPrice(unitPrice);
                persistedItems.add(item);
                total = total.add(unitPrice.multiply(BigDecimal.valueOf(itemRequest.getQuantity())));
            }

            order.setItems(persistedItems);
            order.setTotalAmount(total);
            order.setUpdatedAt(OffsetDateTime.now());
            orderRepository.save(order);

            paymentClient.processPayment(order.getId(), request.getPaymentMethod(), request.getPaymentToken(), total, correlationId);

            order.setStatus(OrderStatus.PAID);
            order.setUpdatedAt(OffsetDateTime.now());
            orderRepository.save(order);

            eventPublisher.publishOrderCreated(order.getId(), order.getUserId(), order.getStatus(), correlationId);
            log.info("Order created successfully orderId={}, correlationId={}", order.getId(), correlationId);
            return toResponse(order, correlationId);
        } catch (Exception ex) {
            log.warn("Saga compensation triggered for orderId={}, reason={}", order.getId(), ex.getMessage());
            compensateInventory(request.getItems(), correlationId);
            order.setStatus(OrderStatus.CANCELLED);
            order.setUpdatedAt(OffsetDateTime.now());
            orderRepository.save(order);
            throw new BusinessException(ex.getMessage());
        }
    }

    @Transactional
    public CancelOrderResponse cancelOrder(Long orderId, String correlationId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));

        if (!isCancellable(order.getStatus())) {
            throw new BusinessException("Order cannot be cancelled in state: " + order.getStatus());
        }

        try {
            if (order.getStatus() == OrderStatus.PAID) {
                paymentClient.refundPayment(orderId, correlationId);
            }
            compensateInventoryFromOrder(order, correlationId);
            order.setStatus(OrderStatus.CANCELLED);
            order.setUpdatedAt(OffsetDateTime.now());
            orderRepository.save(order);
            eventPublisher.publishOrderCancelled(order.getId(), order.getUserId(), correlationId);
        } catch (Exception ex) {
            log.error("Failed to cancel orderId={}, reason={}", orderId, ex.getMessage());
            throw new BusinessException("Failed to cancel order: " + ex.getMessage());
        }

        CancelOrderResponse response = new CancelOrderResponse();
        response.setId(order.getId());
        response.setStatus(order.getStatus());
        response.setCorrelationId(correlationId);
        response.setUpdatedAt(order.getUpdatedAt());
        return response;
    }

    private boolean isCancellable(OrderStatus status) {
        return status == OrderStatus.PENDING || status == OrderStatus.PAID;
    }

    private void compensateInventory(List<OrderItemRequest> items, String correlationId) {
        for (OrderItemRequest item : items) {
            productClient.releaseInventory(item, correlationId);
        }
    }

    private void compensateInventoryFromOrder(Order order, String correlationId) {
        for (OrderItem item : order.getItems()) {
            OrderItemRequest req = new OrderItemRequest();
            req.setProductId(item.getProductId());
            req.setQuantity(item.getQuantity());
            productClient.releaseInventory(req, correlationId);
        }
    }

    private OrderResponse toResponse(Order order, String correlationId) {
        OrderResponse response = new OrderResponse();
        response.setId(order.getId());
        response.setStatus(order.getStatus());
        response.setUserId(order.getUserId());
        response.setTotalAmount(order.getTotalAmount());
        response.setCreatedAt(order.getCreatedAt());
        response.setUpdatedAt(order.getUpdatedAt());
        response.setCorrelationId(correlationId);
        List<OrderItemResponse> items = new ArrayList<>();
        for (OrderItem item : order.getItems()) {
            items.add(new OrderItemResponse(item.getProductId(), item.getQuantity(), item.getUnitPrice()));
        }
        response.setItems(items);
        return response;
    }
}
