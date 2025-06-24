package com.taskflow.taskservice.repository;

import com.taskflow.taskservice.entity.Task;
import com.taskflow.taskservice.entity.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByAssignedTo(Long userId);
    List<Task> findByCreatedBy(Long userId);
    List<Task> findByUserId(Long userId);
    List<Task> findByStatus(TaskStatus status);
} 