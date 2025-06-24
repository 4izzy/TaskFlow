package com.taskflow.taskservice.service;

import com.taskflow.taskservice.dto.TaskDTO;
import com.taskflow.taskservice.entity.Task;
import com.taskflow.taskservice.entity.TaskPriority;
import com.taskflow.taskservice.entity.TaskStatus;
import com.taskflow.taskservice.exception.ResourceNotFoundException;
import com.taskflow.taskservice.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private TaskService taskService;

    private Task testTask;
    private TaskDTO testTaskDTO;

    @BeforeEach
    void setUp() {
        testTask = new Task();
        testTask.setId(1L);
        testTask.setTitle("Test Task");
        testTask.setDescription("Test Description");
        testTask.setStatus(TaskStatus.TODO);
        testTask.setPriority(TaskPriority.MEDIUM);
        testTask.setUserId(1L);
        testTask.setCreatedBy(1L);
        testTask.setCreatedAt(LocalDateTime.now());
        testTask.setDueDate(LocalDateTime.now().plusDays(7));

        testTaskDTO = new TaskDTO();
        testTaskDTO.setId(1L);
        testTaskDTO.setTitle("Test Task");
        testTaskDTO.setDescription("Test Description");
        testTaskDTO.setStatus(TaskStatus.TODO);
        testTaskDTO.setPriority(TaskPriority.MEDIUM);
        testTaskDTO.setUserId(1L);
        testTaskDTO.setCreatedBy(1L);
        testTaskDTO.setCreatedAt(LocalDateTime.now());
        testTaskDTO.setDueDate(LocalDateTime.now().plusDays(7));
    }

    @Test
    void testCreateTask() {
        when(taskRepository.save(any(Task.class))).thenReturn(testTask);

        TaskDTO result = taskService.createTask(testTaskDTO);

        assertNotNull(result);
        assertEquals(testTaskDTO.getTitle(), result.getTitle());
        assertEquals(testTaskDTO.getDescription(), result.getDescription());
        assertEquals(testTaskDTO.getStatus(), result.getStatus());
        assertEquals(testTaskDTO.getPriority(), result.getPriority());
        assertEquals(testTaskDTO.getUserId(), result.getUserId());
        assertEquals(testTaskDTO.getCreatedBy(), result.getCreatedBy());
        assertNotNull(result.getCreatedAt());
        assertEquals(testTaskDTO.getDueDate(), result.getDueDate());
        verify(taskRepository).save(any(Task.class));
        verify(kafkaTemplate).send(eq("task-events"), eq("TASK_CREATED"), any());
    }

    @Test
    void testGetTaskById() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));

        TaskDTO result = taskService.getTaskById(1L);

        assertNotNull(result);
        assertEquals(testTask.getTitle(), result.getTitle());
        assertEquals(testTask.getDescription(), result.getDescription());
        assertEquals(testTask.getStatus(), result.getStatus());
        assertEquals(testTask.getPriority(), result.getPriority());
        assertEquals(testTask.getUserId(), result.getUserId());
        assertEquals(testTask.getCreatedBy(), result.getCreatedBy());
        assertEquals(testTask.getCreatedAt(), result.getCreatedAt());
        assertEquals(testTask.getDueDate(), result.getDueDate());
    }

    @Test
    void testGetAllTasks() {
        List<Task> tasks = Arrays.asList(testTask);
        when(taskRepository.findAll()).thenReturn(tasks);

        List<TaskDTO> result = taskService.getAllTasks();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testTask.getTitle(), result.get(0).getTitle());
        assertEquals(testTask.getDescription(), result.get(0).getDescription());
        assertEquals(testTask.getStatus(), result.get(0).getStatus());
        assertEquals(testTask.getPriority(), result.get(0).getPriority());
        assertEquals(testTask.getUserId(), result.get(0).getUserId());
        assertEquals(testTask.getCreatedBy(), result.get(0).getCreatedBy());
        assertEquals(testTask.getCreatedAt(), result.get(0).getCreatedAt());
        assertEquals(testTask.getDueDate(), result.get(0).getDueDate());
    }

    @Test
    void testGetTasksByUserId() {
        List<Task> tasks = Arrays.asList(testTask);
        when(taskRepository.findByUserId(1L)).thenReturn(tasks);

        List<TaskDTO> result = taskService.getTasksByUserId(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testTask.getTitle(), result.get(0).getTitle());
        assertEquals(testTask.getDescription(), result.get(0).getDescription());
        assertEquals(testTask.getStatus(), result.get(0).getStatus());
        assertEquals(testTask.getPriority(), result.get(0).getPriority());
        assertEquals(testTask.getUserId(), result.get(0).getUserId());
        assertEquals(testTask.getCreatedBy(), result.get(0).getCreatedBy());
        assertEquals(testTask.getCreatedAt(), result.get(0).getCreatedAt());
        assertEquals(testTask.getDueDate(), result.get(0).getDueDate());
    }

    @Test
    void testUpdateTask() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
        when(taskRepository.save(any(Task.class))).thenReturn(testTask);

        TaskDTO updatedDTO = new TaskDTO();
        updatedDTO.setTitle("Updated Task");
        updatedDTO.setDescription("Updated Description");
        updatedDTO.setStatus(TaskStatus.IN_PROGRESS);
        updatedDTO.setPriority(TaskPriority.HIGH);
        updatedDTO.setUserId(1L);
        updatedDTO.setCreatedBy(1L);
        updatedDTO.setDueDate(LocalDateTime.now().plusDays(14));

        TaskDTO result = taskService.updateTask(1L, updatedDTO);

        assertNotNull(result);
        assertEquals(updatedDTO.getTitle(), result.getTitle());
        assertEquals(updatedDTO.getDescription(), result.getDescription());
        assertEquals(updatedDTO.getStatus(), result.getStatus());
        assertEquals(updatedDTO.getPriority(), result.getPriority());
        assertEquals(updatedDTO.getUserId(), result.getUserId());
        assertEquals(updatedDTO.getCreatedBy(), result.getCreatedBy());
        assertEquals(updatedDTO.getDueDate(), result.getDueDate());
        verify(taskRepository).save(any(Task.class));
        verify(kafkaTemplate).send(eq("task-events"), eq("TASK_UPDATED"), any());
    }

    @Test
    void testDeleteTask() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
        doNothing().when(taskRepository).delete(any(Task.class));

        taskService.deleteTask(1L);

        verify(taskRepository).delete(any(Task.class));
        verify(kafkaTemplate).send(eq("task-events"), eq("TASK_DELETED"), any());
    }

    @Test
    void testGetTaskByIdNotFound() {
        when(taskRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> taskService.getTaskById(1L));
    }

    @Test
    void testUpdateTaskNotFound() {
        when(taskRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> taskService.updateTask(1L, testTaskDTO));
    }

    @Test
    void testDeleteTaskNotFound() {
        when(taskRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> taskService.deleteTask(1L));
    }
} 