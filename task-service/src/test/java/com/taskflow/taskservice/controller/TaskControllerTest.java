package com.taskflow.taskservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskflow.taskservice.dto.TaskDTO;
import com.taskflow.taskservice.entity.TaskPriority;
import com.taskflow.taskservice.entity.TaskStatus;
import com.taskflow.taskservice.exception.ResourceNotFoundException;
import com.taskflow.taskservice.service.TaskService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TaskController.class)
public class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TaskService taskService;

    @Test
    public void testCreateTask() throws Exception {
        TaskDTO taskDTO = createTaskDTO(1L, "Test Task", "Test Description", 1L);

        when(taskService.createTask(any(TaskDTO.class))).thenReturn(taskDTO);

        mockMvc.perform(post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(taskDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
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
    public void testGetTaskById() throws Exception {
        TaskDTO taskDTO = createTaskDTO(1L, "Test Task", "Test Description", 1L);

        when(taskService.getTaskById(1L)).thenReturn(taskDTO);

        mockMvc.perform(get("/api/tasks/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
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
    public void testGetTaskByIdNotFound() throws Exception {
        when(taskService.getTaskById(1L)).thenThrow(new ResourceNotFoundException("Task not found"));

        mockMvc.perform(get("/api/tasks/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testGetAllTasks() throws Exception {
        List<TaskDTO> tasks = Arrays.asList(
            createTaskDTO(1L, "Task 1", "Description 1", 1L),
            createTaskDTO(2L, "Task 2", "Description 2", 1L)
        );

        when(taskService.getAllTasks()).thenReturn(tasks);

        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Task 1"))
                .andExpect(jsonPath("$[0].status").value("TODO"))
                .andExpect(jsonPath("$[0].priority").value("MEDIUM"))
                .andExpect(jsonPath("$[1].title").value("Task 2"))
                .andExpect(jsonPath("$[1].status").value("TODO"))
                .andExpect(jsonPath("$[1].priority").value("MEDIUM"));
    }

    @Test
    public void testGetTasksByUserId() throws Exception {
        List<TaskDTO> tasks = Arrays.asList(
            createTaskDTO(1L, "Task 1", "Description 1", 1L),
            createTaskDTO(2L, "Task 2", "Description 2", 1L)
        );

        when(taskService.getTasksByUserId(1L)).thenReturn(tasks);

        mockMvc.perform(get("/api/tasks/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Task 1"))
                .andExpect(jsonPath("$[0].status").value("TODO"))
                .andExpect(jsonPath("$[0].priority").value("MEDIUM"))
                .andExpect(jsonPath("$[1].title").value("Task 2"))
                .andExpect(jsonPath("$[1].status").value("TODO"))
                .andExpect(jsonPath("$[1].priority").value("MEDIUM"));
    }

    @Test
    public void testUpdateTask() throws Exception {
        TaskDTO taskDTO = createTaskDTO(1L, "Updated Task", "Updated Description", 1L);
        taskDTO.setStatus(TaskStatus.IN_PROGRESS);
        taskDTO.setPriority(TaskPriority.HIGH);

        when(taskService.updateTask(eq(1L), any(TaskDTO.class))).thenReturn(taskDTO);

        mockMvc.perform(put("/api/tasks/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(taskDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Task"))
                .andExpect(jsonPath("$.description").value("Updated Description"))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.priority").value("HIGH"));
    }

    @Test
    public void testUpdateTaskNotFound() throws Exception {
        TaskDTO taskDTO = createTaskDTO(1L, "Updated Task", "Updated Description", 1L);

        when(taskService.updateTask(eq(1L), any(TaskDTO.class)))
                .thenThrow(new ResourceNotFoundException("Task not found"));

        mockMvc.perform(put("/api/tasks/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(taskDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testDeleteTask() throws Exception {
        doNothing().when(taskService).deleteTask(1L);

        mockMvc.perform(delete("/api/tasks/1"))
                .andExpect(status().isOk());

        verify(taskService).deleteTask(1L);
    }

    @Test
    public void testDeleteTaskNotFound() throws Exception {
        doThrow(new ResourceNotFoundException("Task not found")).when(taskService).deleteTask(1L);

        mockMvc.perform(delete("/api/tasks/1"))
                .andExpect(status().isNotFound());

        verify(taskService).deleteTask(1L);
    }

    private TaskDTO createTaskDTO(Long id, String title, String description, Long userId) {
        TaskDTO taskDTO = new TaskDTO();
        taskDTO.setId(id);
        taskDTO.setTitle(title);
        taskDTO.setDescription(description);
        taskDTO.setStatus(TaskStatus.TODO);
        taskDTO.setPriority(TaskPriority.MEDIUM);
        taskDTO.setUserId(userId);
        taskDTO.setCreatedBy(userId);
        taskDTO.setCreatedAt(LocalDateTime.now());
        taskDTO.setDueDate(LocalDateTime.now().plusDays(7));
        return taskDTO;
    }
} 