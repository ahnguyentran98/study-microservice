package com.microservices.notificationservice.service;

import com.microservices.notificationservice.dto.NotificationRequest;
import com.microservices.notificationservice.dto.NotificationStatus;
import com.microservices.notificationservice.dto.NotificationType;
import com.microservices.notificationservice.model.Notification;
import com.microservices.notificationservice.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class NotificationService {
    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private JavaMailSender mailSender;

    public Notification sendNotification(NotificationRequest request) {
        Notification notification = new Notification(
            request.getUserId(),
            request.getRecipient(),
            request.getType(),
            request.getSubject(),
            request.getMessage()
        );
        notification.setTemplateData(request.getTemplateData());

        // Save notification to database first
        notification = notificationRepository.save(notification);

        // Send notification based on type
        boolean sent = false;
        try {
            switch (request.getType()) {
                case EMAIL:
                    sent = sendEmail(notification);
                    break;
                case SMS:
                    sent = sendSMS(notification);
                    break;
                case PUSH_NOTIFICATION:
                    sent = sendPushNotification(notification);
                    break;
            }

            if (sent) {
                notification.setStatus(NotificationStatus.SENT);
                notification.setSentAt(LocalDateTime.now());
                logger.info("Notification sent successfully: {}", notification.getId());
            } else {
                notification.setStatus(NotificationStatus.FAILED);
                notification.setFailureReason("Failed to send notification");
                logger.warn("Failed to send notification: {}", notification.getId());
            }
        } catch (Exception e) {
            notification.setStatus(NotificationStatus.FAILED);
            notification.setFailureReason(e.getMessage());
            logger.error("Error sending notification: {}", notification.getId(), e);
        }

        notification.setUpdatedAt(LocalDateTime.now());
        return notificationRepository.save(notification);
    }

    public List<Notification> getNotificationsByUserId(Long userId) {
        return notificationRepository.findByUserId(userId, Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    public List<Notification> getNotificationsByStatus(NotificationStatus status) {
        return notificationRepository.findByStatus(status);
    }

    public Optional<Notification> getNotificationById(String id) {
        return notificationRepository.findById(id);
    }

    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndStatus(userId, NotificationStatus.SENT);
    }

    private boolean sendEmail(Notification notification) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(notification.getRecipient());
            message.setSubject(notification.getSubject());
            message.setText(notification.getMessage());
            message.setFrom("noreply@ecommerce.com");

            mailSender.send(message);
            return true;
        } catch (Exception e) {
            logger.error("Error sending email", e);
            return false;
        }
    }

    private boolean sendSMS(Notification notification) {
        // Simulate SMS sending
        try {
            Thread.sleep(500); // Simulate network delay
            logger.info("SMS sent to {}: {}", notification.getRecipient(), notification.getMessage());
            return Math.random() > 0.1; // 90% success rate
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    private boolean sendPushNotification(Notification notification) {
        // Simulate push notification sending
        try {
            Thread.sleep(200); // Simulate network delay
            logger.info("Push notification sent to user {}: {}", notification.getUserId(), notification.getMessage());
            return Math.random() > 0.05; // 95% success rate
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    public void processOrderCreatedEvent(Map<String, Object> orderEvent) {
        Long userId = Long.valueOf(orderEvent.get("userId").toString());
        Long orderId = Long.valueOf(orderEvent.get("orderId").toString());
        
        // You would typically get user email from user service here
        String userEmail = "user" + userId + "@example.com"; // Placeholder
        
        NotificationRequest emailRequest = new NotificationRequest(
            userId,
            userEmail,
            NotificationType.EMAIL,
            "Order Confirmation",
            "Your order #" + orderId + " has been confirmed and is being processed."
        );
        
        sendNotification(emailRequest);
        logger.info("Order confirmation notification sent for order: {}", orderId);
    }

    public void processPaymentProcessedEvent(Map<String, Object> paymentEvent) {
        Long userId = Long.valueOf(paymentEvent.get("userId").toString());
        Long orderId = Long.valueOf(paymentEvent.get("orderId").toString());
        String status = paymentEvent.get("status").toString();
        
        String userEmail = "user" + userId + "@example.com"; // Placeholder
        
        String subject, message;
        if ("COMPLETED".equals(status)) {
            subject = "Payment Successful";
            message = "Your payment for order #" + orderId + " has been processed successfully.";
        } else {
            subject = "Payment Failed";
            message = "Your payment for order #" + orderId + " failed. Please try again.";
        }
        
        NotificationRequest emailRequest = new NotificationRequest(
            userId,
            userEmail,
            NotificationType.EMAIL,
            subject,
            message
        );
        
        sendNotification(emailRequest);
        logger.info("Payment notification sent for order: {}", orderId);
    }
}