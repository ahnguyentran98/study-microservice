package com.example.order_service.service.event;

import com.example.order_service.enums.OrderStatus;
import java.time.OffsetDateTime;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class OrderEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public OrderEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishOrderCreated(Long orderId, Long userId, OrderStatus status, String correlationId) {
        OrderEventPayload payload = new OrderEventPayload(orderId, userId, status.name(), OffsetDateTime.now(), correlationId);
        rabbitTemplate.convertAndSend("order.exchange", "order.created", payload);
    }

    public void publishOrderCancelled(Long orderId, Long userId, String correlationId) {
        OrderEventPayload payload = new OrderEventPayload(orderId, userId, OrderStatus.CANCELLED.name(), OffsetDateTime.now(), correlationId);
        rabbitTemplate.convertAndSend("order.exchange", "order.cancelled", payload);
    }

    public record OrderEventPayload(Long orderId, Long userId, String status, OffsetDateTime timestamp, String correlationId) {}
}
