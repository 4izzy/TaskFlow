package com.taskflow.notificationservice.dto;

import com.taskflow.notificationservice.entity.NotificationType;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDTO {
    private Long id;
    private String message;
    private Long userId;
    private NotificationType type;
    private boolean read;
    private LocalDateTime createdAt;
} 