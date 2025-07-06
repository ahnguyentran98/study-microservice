package com.microservices.orderservice.messaging;

import com.microservices.orderservice.dto.OrderStatus;
import com.microservices.orderservice.model.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class OrderEventPublisher {
    private static final Logger logger = LoggerFactory.getLogger(OrderEventPublisher.class);

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void publishOrderCreated(Order order) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", "ORDER_CREATED");
        event.put("orderId", order.getId());
        event.put("userId", order.getUserId());
        event.put("totalAmount", order.getTotalAmount());
        event.put("status", order.getStatus().toString());
        event.put("timestamp", order.getCreatedAt().toString());

        rabbitTemplate.convertAndSend("order.exchange", "order.created", event);
        logger.info("Published ORDER_CREATED event for order: {}", order.getId());
    }

    public void publishOrderStatusChanged(Order order, OrderStatus oldStatus, OrderStatus newStatus) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", "ORDER_STATUS_CHANGED");
        event.put("orderId", order.getId());
        event.put("userId", order.getUserId());
        event.put("oldStatus", oldStatus.toString());
        event.put("newStatus", newStatus.toString());
        event.put("timestamp", order.getUpdatedAt().toString());

        rabbitTemplate.convertAndSend("order.exchange", "order.status.changed", event);
        logger.info("Published ORDER_STATUS_CHANGED event for order: {} ({} -> {})", 
                   order.getId(), oldStatus, newStatus);
    }

    public void publishOrderCancelled(Order order) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", "ORDER_CANCELLED");
        event.put("orderId", order.getId());
        event.put("userId", order.getUserId());
        event.put("totalAmount", order.getTotalAmount());
        event.put("timestamp", order.getUpdatedAt().toString());

        rabbitTemplate.convertAndSend("order.exchange", "order.cancelled", event);
        logger.info("Published ORDER_CANCELLED event for order: {}", order.getId());
    }
}