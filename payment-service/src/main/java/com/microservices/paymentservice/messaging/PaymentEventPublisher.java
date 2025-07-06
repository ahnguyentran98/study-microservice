package com.microservices.paymentservice.messaging;

import com.microservices.paymentservice.model.Payment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class PaymentEventPublisher {
    private static final Logger logger = LoggerFactory.getLogger(PaymentEventPublisher.class);

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void publishPaymentProcessed(Payment payment) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", "PAYMENT_PROCESSED");
        event.put("paymentId", payment.getId());
        event.put("orderId", payment.getOrderId());
        event.put("userId", payment.getUserId());
        event.put("amount", payment.getAmount());
        event.put("status", payment.getStatus().toString());
        event.put("paymentReference", payment.getPaymentReference());
        event.put("timestamp", payment.getUpdatedAt().toString());

        rabbitTemplate.convertAndSend("payment.exchange", "payment.processed", event);
        logger.info("Published PAYMENT_PROCESSED event for payment: {}", payment.getId());
    }

    public void publishPaymentRefunded(Payment payment) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", "PAYMENT_REFUNDED");
        event.put("paymentId", payment.getId());
        event.put("orderId", payment.getOrderId());
        event.put("userId", payment.getUserId());
        event.put("amount", payment.getAmount());
        event.put("paymentReference", payment.getPaymentReference());
        event.put("timestamp", payment.getUpdatedAt().toString());

        rabbitTemplate.convertAndSend("payment.exchange", "payment.refunded", event);
        logger.info("Published PAYMENT_REFUNDED event for payment: {}", payment.getId());
    }
}