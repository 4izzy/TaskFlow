package com.taskflow.taskservice.service;

import com.taskflow.taskservice.dto.TaskDTO;
import com.taskflow.taskservice.entity.Task;
import com.taskflow.taskservice.exception.ResourceNotFoundException;
import com.taskflow.taskservice.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskService {
    private final TaskRepository taskRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Transactional
    public TaskDTO createTask(TaskDTO taskDTO) {
        log.info("Creating new task: {}", taskDTO.getTitle());
        Task task = convertToEntity(taskDTO);
        Task savedTask = taskRepository.save(task);
        log.info("Task created successfully with id: {}", savedTask.getId());

        TaskDTO createdTaskDTO = convertToDTO(savedTask);
        kafkaTemplate.send("task-events", "TASK_CREATED", createdTaskDTO);
        return createdTaskDTO;
    }

    @Transactional(readOnly = true)
    public TaskDTO getTaskById(Long id) {
        log.info("Getting task by id: {}", id);
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));
        return convertToDTO(task);
    }

    @Transactional(readOnly = true)
    public List<TaskDTO> getAllTasks() {
        log.info("Getting all tasks");
        return taskRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TaskDTO> getTasksByUserId(Long userId) {
        log.info("Getting tasks for user: {}", userId);
        return taskRepository.findByUserId(userId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public TaskDTO updateTask(Long id, TaskDTO taskDTO) {
        log.info("Updating task with id: {}", id);
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));

        task.setTitle(taskDTO.getTitle());
        task.setDescription(taskDTO.getDescription());
        task.setStatus(taskDTO.getStatus());
        task.setPriority(taskDTO.getPriority());
        task.setAssignedTo(taskDTO.getAssignedTo());
        task.setDueDate(taskDTO.getDueDate());

        Task updatedTask = taskRepository.save(task);
        log.info("Task updated successfully");

        TaskDTO updatedTaskDTO = convertToDTO(updatedTask);
        kafkaTemplate.send("task-events", "TASK_UPDATED", updatedTaskDTO);
        return updatedTaskDTO;
    }

    @Transactional
    public void deleteTask(Long id) {
        log.info("Deleting task with id: {}", id);
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));

        taskRepository.delete(task);
        log.info("Task deleted successfully");
        kafkaTemplate.send("task-events", "TASK_DELETED", convertToDTO(task));
    }

    private Task convertToEntity(TaskDTO dto) {
        Task task = new Task();
        task.setId(dto.getId());
        task.setTitle(dto.getTitle());
        task.setDescription(dto.getDescription());
        task.setStatus(dto.getStatus());
        task.setPriority(dto.getPriority());
        task.setAssignedTo(dto.getAssignedTo());
        task.setCreatedBy(dto.getCreatedBy());
        task.setUserId(dto.getUserId());
        task.setDueDate(dto.getDueDate());
        return task;
    }

    private TaskDTO convertToDTO(Task task) {
        TaskDTO dto = new TaskDTO();
        dto.setId(task.getId());
        dto.setTitle(task.getTitle());
        dto.setDescription(task.getDescription());
        dto.setStatus(task.getStatus());
        dto.setPriority(task.getPriority());
        dto.setAssignedTo(task.getAssignedTo());
        dto.setCreatedBy(task.getCreatedBy());
        dto.setUserId(task.getUserId());
        dto.setCreatedAt(task.getCreatedAt());
        dto.setUpdatedAt(task.getUpdatedAt());
        dto.setDueDate(task.getDueDate());
        return dto;
    }
} 