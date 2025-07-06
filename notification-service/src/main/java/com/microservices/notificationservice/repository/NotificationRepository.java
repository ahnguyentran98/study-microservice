package com.microservices.notificationservice.repository;

import com.microservices.notificationservice.dto.NotificationStatus;
import com.microservices.notificationservice.dto.NotificationType;
import com.microservices.notificationservice.model.Notification;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends MongoRepository<Notification, String> {
    List<Notification> findByUserId(Long userId, Sort sort);
    List<Notification> findByStatus(NotificationStatus status);
    List<Notification> findByType(NotificationType type);
    List<Notification> findByUserIdAndStatus(Long userId, NotificationStatus status);
    List<Notification> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
    long countByUserIdAndStatus(Long userId, NotificationStatus status);
}