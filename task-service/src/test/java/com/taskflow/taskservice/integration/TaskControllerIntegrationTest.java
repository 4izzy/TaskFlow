package com.taskflow.taskservice.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskflow.taskservice.dto.TaskDTO;
import com.taskflow.taskservice.entity.TaskPriority;
import com.taskflow.taskservice.entity.TaskStatus;
import com.taskflow.taskservice.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@EmbeddedKafka(partitions = 1, topics = {"task-events"})
@ActiveProfiles("test")
@Transactional
public class TaskControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TaskRepository taskRepository;

    private TaskDTO testTaskDTO;

    @BeforeEach
    void setUp() {
        testTaskDTO = new TaskDTO();
        testTaskDTO.setTitle("Test Task");
        testTaskDTO.setDescription("Test Description");
        testTaskDTO.setStatus(TaskStatus.TODO);
        testTaskDTO.setPriority(TaskPriority.MEDIUM);
        testTaskDTO.setUserId(1L);
        testTaskDTO.setCreatedBy(1L);
        testTaskDTO.setDueDate(LocalDateTime.now().plusDays(7));
    }

    @Test
    void testCreateTask() throws Exception {
        mockMvc.perform(post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testTaskDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title").value("Test Task"))
                .andExpect(jsonPath("$.description").value("Test Description"))
                .andExpect(jsonPath("$.status").value("TODO"))
                .andExpect(jsonPath("$.priority").value("MEDIUM"))
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.createdBy").value(1))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.dueDate").exists());
    }

    @Test
    void testGetTaskById() throws Exception {
        TaskDTO createdTask = createTask();

        mockMvc.perform(get("/api/tasks/" + createdTask.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdTask.getId()))
                .andExpect(jsonPath("$.title").value("Test Task"))
                .andExpect(jsonPath("$.description").value("Test Description"))
                .andExpect(jsonPath("$.status").value("TODO"))
                .andExpect(jsonPath("$.priority").value("MEDIUM"))
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.createdBy").value(1))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.dueDate").exists());
    }

    @Test
    void testGetTaskByIdNotFound() throws Exception {
        mockMvc.perform(get("/api/tasks/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetAllTasks() throws Exception {
        createTask();
        createTask();

        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].title").value("Test Task"))
                .andExpect(jsonPath("$[1].title").value("Test Task"));
    }

    @Test
    void testGetTasksByUserId() throws Exception {
        createTask();
        createTask();

        mockMvc.perform(get("/api/tasks/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].title").value("Test Task"))
                .andExpect(jsonPath("$[1].title").value("Test Task"));
    }

    @Test
    void testUpdateTask() throws Exception {
        TaskDTO createdTask = createTask();

        TaskDTO updatedTaskDTO = new TaskDTO();
        updatedTaskDTO.setTitle("Updated Task");
        updatedTaskDTO.setDescription("Updated Description");
        updatedTaskDTO.setStatus(TaskStatus.IN_PROGRESS);
        updatedTaskDTO.setPriority(TaskPriority.HIGH);
        updatedTaskDTO.setUserId(1L);
        updatedTaskDTO.setCreatedBy(1L);
        updatedTaskDTO.setDueDate(LocalDateTime.now().plusDays(14));

        mockMvc.perform(put("/api/tasks/" + createdTask.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedTaskDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdTask.getId()))
                .andExpect(jsonPath("$.title").value("Updated Task"))
                .andExpect(jsonPath("$.description").value("Updated Description"))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.priority").value("HIGH"));
    }

    @Test
    void testUpdateTaskNotFound() throws Exception {
        TaskDTO updatedTaskDTO = new TaskDTO();
        updatedTaskDTO.setTitle("Updated Task");

        mockMvc.perform(put("/api/tasks/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedTaskDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeleteTask() throws Exception {
        TaskDTO createdTask = createTask();

        mockMvc.perform(delete("/api/tasks/" + createdTask.getId()))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/tasks/" + createdTask.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeleteTaskNotFound() throws Exception {
        mockMvc.perform(delete("/api/tasks/999"))
                .andExpect(status().isNotFound());
    }

    private TaskDTO createTask() throws Exception {
        String response = mockMvc.perform(post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testTaskDTO)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readValue(response, TaskDTO.class);
    }
} 