package com.taskflow.taskservice.controller;

import com.taskflow.taskservice.dto.TaskDTO;
import com.taskflow.taskservice.exception.ResourceNotFoundException;
import com.taskflow.taskservice.service.TaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {
    private final TaskService taskService;

    @PostMapping
    public ResponseEntity<TaskDTO> createTask(@RequestBody TaskDTO taskDTO) {
        log.info("Received request to create task: {}", taskDTO.getTitle());
        return ResponseEntity.ok(taskService.createTask(taskDTO));
    }

    @GetMapping
    public ResponseEntity<List<TaskDTO>> getAllTasks() {
        log.info("Received request to get all tasks");
        return ResponseEntity.ok(taskService.getAllTasks());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskDTO> getTaskById(@PathVariable Long id) {
        log.info("Received request to get task with id: {}", id);
        return ResponseEntity.ok(taskService.getTaskById(id));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<TaskDTO>> getTasksByUserId(@PathVariable Long userId) {
        log.info("Received request to get tasks for user: {}", userId);
        return ResponseEntity.ok(taskService.getTasksByUserId(userId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TaskDTO> updateTask(@PathVariable Long id, @RequestBody TaskDTO taskDTO) {
        log.info("Received request to update task with id: {}", id);
        return ResponseEntity.ok(taskService.updateTask(id, taskDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        log.info("Received request to delete task with id: {}", id);
        taskService.deleteTask(id);
        return ResponseEntity.ok().build();
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<String> handleResourceNotFoundException(ResourceNotFoundException ex) {
        log.error("Resource not found: {}", ex.getMessage());
        return ResponseEntity.notFound().build();
    }
} 