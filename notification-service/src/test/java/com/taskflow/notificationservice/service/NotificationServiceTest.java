package com.taskflow.notificationservice.service;

import com.taskflow.notificationservice.dto.NotificationDTO;
import com.taskflow.notificationservice.entity.Notification;
import com.taskflow.notificationservice.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationService notificationService;

    private Notification testNotification;
    private NotificationDTO testNotificationDTO;

    @BeforeEach
    void setUp() {
        testNotification = new Notification();
        testNotification.setId(1L);
        testNotification.setMessage("Test Notification");
        testNotification.setUserId(1L);
        testNotification.setType("TEST");
        testNotification.setRead(false);

        testNotificationDTO = new NotificationDTO();
        testNotificationDTO.setId(1L);
        testNotificationDTO.setMessage("Test Notification");
        testNotificationDTO.setUserId(1L);
        testNotificationDTO.setType("TEST");
        testNotificationDTO.setRead(false);
    }

    @Test
    void testHandleUserEvent() {
        notificationService.handleUserEvent("USER_CREATED", "test payload");
        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    void testHandleTaskEvent() {
        notificationService.handleTaskEvent("TASK_CREATED", "test payload");
        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    void testGetUserNotifications() {
        List<Notification> notifications = Arrays.asList(testNotification);
        when(notificationRepository.findByUserIdOrderByCreatedAtDesc(1L)).thenReturn(notifications);

        List<NotificationDTO> result = notificationService.getUserNotifications(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testNotification.getMessage(), result.get(0).getMessage());
    }

    @Test
    void testGetUnreadUserNotifications() {
        List<Notification> notifications = Arrays.asList(testNotification);
        when(notificationRepository.findByUserIdAndReadOrderByCreatedAtDesc(1L, false)).thenReturn(notifications);

        List<NotificationDTO> result = notificationService.getUnreadUserNotifications(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testNotification.getMessage(), result.get(0).getMessage());
    }

    @Test
    void testMarkNotificationAsRead() {
        when(notificationRepository.findById(1L)).thenReturn(java.util.Optional.of(testNotification));
        when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);

        notificationService.markNotificationAsRead(1L);

        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    void testMarkAllNotificationsAsRead() {
        List<Notification> notifications = Arrays.asList(testNotification);
        when(notificationRepository.findByUserIdAndReadOrderByCreatedAtDesc(1L, false)).thenReturn(notifications);
        when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);

        notificationService.markAllNotificationsAsRead(1L);

        verify(notificationRepository).save(any(Notification.class));
    }
} 