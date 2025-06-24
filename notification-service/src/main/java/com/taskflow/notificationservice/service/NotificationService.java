package com.taskflow.notificationservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskflow.notificationservice.dto.NotificationDTO;
import com.taskflow.notificationservice.entity.Notification;
import com.taskflow.notificationservice.entity.NotificationType;
import com.taskflow.notificationservice.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "user-events", groupId = "notification-service-group")
    public void handleUserEvent(String eventType, String payload) {
        log.info("Received user event: {} with payload: {}", eventType, payload);
        
        try {
            switch (eventType) {
                case "USER_CREATED":
                    handleUserCreated(payload);
                    break;
                case "USER_UPDATED":
                    handleUserUpdated(payload);
                    break;
                case "USER_DELETED":
                    handleUserDeleted(payload);
                    break;
                default:
                    log.warn("Unknown user event type: {}", eventType);
            }
        } catch (Exception e) {
            log.error("Error processing user event: {}", e.getMessage(), e);
        }
    }

    @KafkaListener(topics = "task-events", groupId = "notification-service-group")
    public void handleTaskEvent(String eventType, String payload) {
        log.info("Received task event: {} with payload: {}", eventType, payload);
        
        try {
            switch (eventType) {
                case "TASK_CREATED":
                    handleTaskCreated(payload);
                    break;
                case "TASK_UPDATED":
                    handleTaskUpdated(payload);
                    break;
                case "TASK_DELETED":
                    handleTaskDeleted(payload);
                    break;
                default:
                    log.warn("Unknown task event type: {}", eventType);
            }
        } catch (Exception e) {
            log.error("Error processing task event: {}", e.getMessage(), e);
        }
    }

    @Transactional(readOnly = true)
    public List<NotificationDTO> getUserNotifications(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<NotificationDTO> getUnreadUserNotifications(Long userId) {
        return notificationRepository.findByUserIdAndReadOrderByCreatedAtDesc(userId, false)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void markNotificationAsRead(Long notificationId) {
        notificationRepository.findById(notificationId).ifPresent(notification -> {
            notification.setRead(true);
            notificationRepository.save(notification);
        });
    }

    @Transactional
    public void markAllNotificationsAsRead(Long userId) {
        notificationRepository.findByUserIdAndReadOrderByCreatedAtDesc(userId, false)
                .forEach(notification -> {
                    notification.setRead(true);
                    notificationRepository.save(notification);
                });
    }

    private void handleUserCreated(String payload) {
        try {
            Notification notification = new Notification();
            notification.setMessage("New user has been created");
            notification.setType(NotificationType.USER_CREATED);
            notification.setUserId(extractUserIdFromPayload(payload));
            saveNotification(notification);
        } catch (Exception e) {
            log.error("Error handling user created event: {}", e.getMessage(), e);
        }
    }

    private void handleUserUpdated(String payload) {
        try {
            Notification notification = new Notification();
            notification.setMessage("User profile has been updated");
            notification.setType(NotificationType.USER_UPDATED);
            notification.setUserId(extractUserIdFromPayload(payload));
            saveNotification(notification);
        } catch (Exception e) {
            log.error("Error handling user updated event: {}", e.getMessage(), e);
        }
    }

    private void handleUserDeleted(String payload) {
        try {
            Notification notification = new Notification();
            notification.setMessage("User has been deleted");
            notification.setType(NotificationType.USER_DELETED);
            notification.setUserId(extractUserIdFromPayload(payload));
            saveNotification(notification);
        } catch (Exception e) {
            log.error("Error handling user deleted event: {}", e.getMessage(), e);
        }
    }

    private void handleTaskCreated(String payload) {
        try {
            Notification notification = new Notification();
            notification.setMessage("New task has been created");
            notification.setType(NotificationType.TASK_CREATED);
            notification.setUserId(extractUserIdFromPayload(payload));
            saveNotification(notification);
        } catch (Exception e) {
            log.error("Error handling task created event: {}", e.getMessage(), e);
        }
    }

    private void handleTaskUpdated(String payload) {
        try {
            Notification notification = new Notification();
            notification.setMessage("Task has been updated");
            notification.setType(NotificationType.TASK_UPDATED);
            notification.setUserId(extractUserIdFromPayload(payload));
            saveNotification(notification);
        } catch (Exception e) {
            log.error("Error handling task updated event: {}", e.getMessage(), e);
        }
    }

    private void handleTaskDeleted(String payload) {
        try {
            Notification notification = new Notification();
            notification.setMessage("Task has been deleted");
            notification.setType(NotificationType.TASK_DELETED);
            notification.setUserId(extractUserIdFromPayload(payload));
            saveNotification(notification);
        } catch (Exception e) {
            log.error("Error handling task deleted event: {}", e.getMessage(), e);
        }
    }

    private Long extractUserIdFromPayload(String payload) {
        try {
            // Предполагаем, что payload содержит JSON с полем userId
            return objectMapper.readTree(payload).get("userId").asLong();
        } catch (Exception e) {
            log.error("Error extracting userId from payload: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to extract userId from payload", e);
        }
    }

    private void saveNotification(Notification notification) {
        try {
            notificationRepository.save(notification);
            log.info("Notification saved: {}", notification);
            // TODO: Добавить отправку уведомлений через email, push и т.д.
        } catch (Exception e) {
            log.error("Error saving notification: {}", e.getMessage(), e);
        }
    }

    private NotificationDTO convertToDTO(Notification notification) {
        NotificationDTO dto = new NotificationDTO();
        dto.setId(notification.getId());
        dto.setMessage(notification.getMessage());
        dto.setUserId(notification.getUserId());
        dto.setType(notification.getType());
        dto.setRead(notification.isRead());
        dto.setCreatedAt(notification.getCreatedAt());
        return dto;
    }
} 