package com.microservices.orderservice.service;

import com.microservices.orderservice.dto.OrderItemRequest;
import com.microservices.orderservice.dto.OrderRequest;
import com.microservices.orderservice.dto.OrderStatus;
import com.microservices.orderservice.messaging.OrderEventPublisher;
import com.microservices.orderservice.model.Order;
import com.microservices.orderservice.model.OrderItem;
import com.microservices.orderservice.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class OrderService {
    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private WebClient.Builder webClientBuilder;

    @Autowired
    private OrderEventPublisher orderEventPublisher;

    public Order createOrder(OrderRequest orderRequest) {
        Order order = new Order();
        order.setUserId(orderRequest.getUserId());
        order.setShippingAddress(orderRequest.getShippingAddress());
        order.setBillingAddress(orderRequest.getBillingAddress());
        order.setPaymentMethod(orderRequest.getPaymentMethod());
        order.setStatus(OrderStatus.PENDING);

        BigDecimal totalAmount = BigDecimal.ZERO;
        
        for (OrderItemRequest itemRequest : orderRequest.getItems()) {
            if (!checkProductAvailability(itemRequest.getProductId(), itemRequest.getQuantity())) {
                throw new RuntimeException("Product not available: " + itemRequest.getProductId());
            }
            
            Map<String, Object> productInfo = getProductInfo(itemRequest.getProductId());
            BigDecimal price = new BigDecimal(productInfo.get("price").toString());
            String productName = productInfo.get("name").toString();
            
            OrderItem orderItem = new OrderItem(
                itemRequest.getProductId(),
                productName,
                price,
                itemRequest.getQuantity()
            );
            orderItem.setOrder(order);
            order.getOrderItems().add(orderItem);
            
            totalAmount = totalAmount.add(orderItem.getSubtotal());
        }
        
        order.setTotalAmount(totalAmount);
        Order savedOrder = orderRepository.save(order);
        
        // Update product stock
        for (OrderItemRequest itemRequest : orderRequest.getItems()) {
            updateProductStock(itemRequest.getProductId(), itemRequest.getQuantity());
        }
        
        // Publish order created event
        orderEventPublisher.publishOrderCreated(savedOrder);
        
        logger.info("Order created successfully with ID: {}", savedOrder.getId());
        return savedOrder;
    }

    public List<Order> getOrdersByUserId(Long userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public Optional<Order> getOrderById(Long orderId) {
        return orderRepository.findById(orderId);
    }

    public List<Order> getOrdersByStatus(OrderStatus status) {
        return orderRepository.findByStatus(status);
    }

    public Order updateOrderStatus(Long orderId, OrderStatus newStatus) {
        Optional<Order> optionalOrder = orderRepository.findById(orderId);
        if (optionalOrder.isPresent()) {
            Order order = optionalOrder.get();
            OrderStatus oldStatus = order.getStatus();
            order.setStatus(newStatus);
            Order updatedOrder = orderRepository.save(order);
            
            // Publish order status changed event
            orderEventPublisher.publishOrderStatusChanged(updatedOrder, oldStatus, newStatus);
            
            logger.info("Order {} status changed from {} to {}", orderId, oldStatus, newStatus);
            return updatedOrder;
        }
        throw new RuntimeException("Order not found: " + orderId);
    }

    public void cancelOrder(Long orderId) {
        Optional<Order> optionalOrder = orderRepository.findById(orderId);
        if (optionalOrder.isPresent()) {
            Order order = optionalOrder.get();
            if (order.getStatus() == OrderStatus.PENDING || order.getStatus() == OrderStatus.CONFIRMED) {
                order.setStatus(OrderStatus.CANCELLED);
                orderRepository.save(order);
                
                // Restore product stock
                for (OrderItem item : order.getOrderItems()) {
                    restoreProductStock(item.getProductId(), item.getQuantity());
                }
                
                orderEventPublisher.publishOrderCancelled(order);
                logger.info("Order {} cancelled successfully", orderId);
            } else {
                throw new RuntimeException("Cannot cancel order in current status: " + order.getStatus());
            }
        }
    }

    private boolean checkProductAvailability(Long productId, Integer quantity) {
        try {
            Map<String, Object> response = webClientBuilder.build()
                .get()
                .uri("http://product-service:8080/api/products/{id}/availability?quantity={quantity}", 
                     productId, quantity)
                .retrieve()
                .bodyToMono(Map.class)
                .block();
            
            return response != null && (Boolean) response.get("available");
        } catch (Exception e) {
            logger.error("Error checking product availability", e);
            return false;
        }
    }

    private Map<String, Object> getProductInfo(Long productId) {
        try {
            return webClientBuilder.build()
                .get()
                .uri("http://product-service:8080/api/products/{id}", productId)
                .retrieve()
                .bodyToMono(Map.class)
                .block();
        } catch (Exception e) {
            logger.error("Error getting product info", e);
            throw new RuntimeException("Failed to get product information");
        }
    }

    private void updateProductStock(Long productId, Integer quantity) {
        try {
            webClientBuilder.build()
                .put()
                .uri("http://product-service:8080/api/products/{id}/stock?quantity={quantity}", 
                     productId, quantity)
                .retrieve()
                .bodyToMono(Map.class)
                .block();
        } catch (Exception e) {
            logger.error("Error updating product stock", e);
        }
    }

    private void restoreProductStock(Long productId, Integer quantity) {
        try {
            webClientBuilder.build()
                .put()
                .uri("http://product-service:8080/api/products/{id}/stock?quantity={quantity}", 
                     productId, -quantity)
                .retrieve()
                .bodyToMono(Map.class)
                .block();
        } catch (Exception e) {
            logger.error("Error restoring product stock", e);
        }
    }
}