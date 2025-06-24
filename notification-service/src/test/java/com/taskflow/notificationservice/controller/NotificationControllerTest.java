package com.taskflow.notificationservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskflow.notificationservice.dto.NotificationDTO;
import com.taskflow.notificationservice.entity.NotificationType;
import com.taskflow.notificationservice.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NotificationController.class)
public class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private NotificationService notificationService;

    @Test
    public void testGetUserNotifications() throws Exception {
        List<NotificationDTO> notifications = Arrays.asList(
            createNotificationDTO(1L, "Test Notification 1", 1L, "TEST", false),
            createNotificationDTO(2L, "Test Notification 2", 1L, "TEST", false)
        );

        when(notificationService.getUserNotifications(1L)).thenReturn(notifications);

        mockMvc.perform(get("/api/notifications/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].message").value("Test Notification 1"))
                .andExpect(jsonPath("$[1].message").value("Test Notification 2"));
    }

    @Test
    public void testGetUnreadUserNotifications() throws Exception {
        List<NotificationDTO> notifications = Arrays.asList(
            createNotificationDTO(1L, "Test Notification 1", 1L, "TEST", false),
            createNotificationDTO(2L, "Test Notification 2", 1L, "TEST", false)
        );

        when(notificationService.getUnreadUserNotifications(1L)).thenReturn(notifications);

        mockMvc.perform(get("/api/notifications/user/1/unread"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].message").value("Test Notification 1"))
                .andExpect(jsonPath("$[1].message").value("Test Notification 2"));
    }

    @Test
    public void testMarkNotificationAsRead() throws Exception {
        mockMvc.perform(put("/api/notifications/1/read"))
                .andExpect(status().isOk());
    }

    @Test
    public void testMarkAllNotificationsAsRead() throws Exception {
        mockMvc.perform(put("/api/notifications/user/1/read-all"))
                .andExpect(status().isOk());
    }

    private NotificationDTO createNotificationDTO(Long id, String message, Long userId, String type, boolean read) {
        NotificationDTO dto = new NotificationDTO();
        dto.setId(id);
        dto.setMessage(message);
        dto.setUserId(userId);
        dto.setType(NotificationType.valueOf(type));
        dto.setRead(read);
        return dto;
    }
} 