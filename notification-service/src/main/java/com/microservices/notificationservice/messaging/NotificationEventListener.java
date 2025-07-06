package com.microservices.notificationservice.messaging;

import com.microservices.notificationservice.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class NotificationEventListener {
    private static final Logger logger = LoggerFactory.getLogger(NotificationEventListener.class);

    @Autowired
    private NotificationService notificationService;

    @RabbitListener(queues = "order.created.queue")
    public void handleOrderCreatedEvent(Map<String, Object> event) {
        logger.info("Received ORDER_CREATED event: {}", event);
        try {
            notificationService.processOrderCreatedEvent(event);
        } catch (Exception e) {
            logger.error("Error processing ORDER_CREATED event", e);
        }
    }

    @RabbitListener(queues = "order.status.changed.queue")
    public void handleOrderStatusChangedEvent(Map<String, Object> event) {
        logger.info("Received ORDER_STATUS_CHANGED event: {}", event);
        // Handle order status change notifications
        // Implementation similar to order created
    }

    @RabbitListener(queues = "payment.processed.queue")
    public void handlePaymentProcessedEvent(Map<String, Object> event) {
        logger.info("Received PAYMENT_PROCESSED event: {}", event);
        try {
            notificationService.processPaymentProcessedEvent(event);
        } catch (Exception e) {
            logger.error("Error processing PAYMENT_PROCESSED event", e);
        }
    }

    @RabbitListener(queues = "payment.refunded.queue")
    public void handlePaymentRefundedEvent(Map<String, Object> event) {
        logger.info("Received PAYMENT_REFUNDED event: {}", event);
        // Handle payment refund notifications
        // Implementation similar to payment processed
    }
}