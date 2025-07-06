package com.microservices.paymentservice.service;

import com.microservices.paymentservice.dto.PaymentRequest;
import com.microservices.paymentservice.dto.PaymentStatus;
import com.microservices.paymentservice.messaging.PaymentEventPublisher;
import com.microservices.paymentservice.model.Payment;
import com.microservices.paymentservice.repository.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class PaymentService {
    private static final Logger logger = LoggerFactory.getLogger(PaymentService.class);

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private PaymentEventPublisher paymentEventPublisher;

    public Payment processPayment(PaymentRequest paymentRequest) {
        // Check if payment already exists for this order
        Optional<Payment> existingPayment = paymentRepository.findByOrderId(paymentRequest.getOrderId());
        if (existingPayment.isPresent()) {
            throw new RuntimeException("Payment already exists for order: " + paymentRequest.getOrderId());
        }

        Payment payment = new Payment(
            paymentRequest.getOrderId(),
            paymentRequest.getUserId(),
            paymentRequest.getAmount(),
            paymentRequest.getPaymentMethod()
        );

        payment.setStatus(PaymentStatus.PROCESSING);
        payment = paymentRepository.save(payment);

        // Simulate payment processing
        boolean paymentSuccessful = simulatePaymentProcessing(paymentRequest);

        if (paymentSuccessful) {
            payment.setStatus(PaymentStatus.COMPLETED);
            payment.setPaymentReference(generatePaymentReference());
            logger.info("Payment processed successfully for order: {}", paymentRequest.getOrderId());
        } else {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setFailureReason("Payment processing failed");
            logger.warn("Payment failed for order: {}", paymentRequest.getOrderId());
        }

        payment = paymentRepository.save(payment);
        paymentEventPublisher.publishPaymentProcessed(payment);

        return payment;
    }

    public Optional<Payment> getPaymentByOrderId(Long orderId) {
        return paymentRepository.findByOrderId(orderId);
    }

    public List<Payment> getPaymentsByUserId(Long userId) {
        return paymentRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public Payment refundPayment(Long paymentId) {
        Optional<Payment> optionalPayment = paymentRepository.findById(paymentId);
        if (optionalPayment.isPresent()) {
            Payment payment = optionalPayment.get();
            if (payment.getStatus() == PaymentStatus.COMPLETED) {
                // Simulate refund processing
                boolean refundSuccessful = simulateRefundProcessing(payment);
                
                if (refundSuccessful) {
                    payment.setStatus(PaymentStatus.REFUNDED);
                    payment = paymentRepository.save(payment);
                    
                    paymentEventPublisher.publishPaymentRefunded(payment);
                    logger.info("Payment refunded successfully for ID: {}", paymentId);
                    return payment;
                } else {
                    throw new RuntimeException("Refund processing failed");
                }
            } else {
                throw new RuntimeException("Cannot refund payment with status: " + payment.getStatus());
            }
        }
        throw new RuntimeException("Payment not found: " + paymentId);
    }

    public List<Payment> getPaymentsByStatus(PaymentStatus status) {
        return paymentRepository.findByStatus(status);
    }

    private boolean simulatePaymentProcessing(PaymentRequest paymentRequest) {
        try {
            // Simulate network delay
            Thread.sleep(1000);
            
            // Simulate success rate of 90%
            return Math.random() > 0.1;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    private boolean simulateRefundProcessing(Payment payment) {
        try {
            // Simulate network delay
            Thread.sleep(500);
            
            // Simulate success rate of 95% for refunds
            return Math.random() > 0.05;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    private String generatePaymentReference() {
        return "PAY-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}