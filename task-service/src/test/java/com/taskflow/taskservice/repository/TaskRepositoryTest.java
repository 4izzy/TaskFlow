package com.taskflow.taskservice.repository;

import com.taskflow.taskservice.entity.Task;
import com.taskflow.taskservice.entity.TaskPriority;
import com.taskflow.taskservice.entity.TaskStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
public class TaskRepositoryTest {

    @Autowired
    private TaskRepository taskRepository;

    @Test
    void testSaveTask() {
        Task task = createTask("Test Task", "Test Description", 1L);
        Task savedTask = taskRepository.save(task);

        assertThat(savedTask).isNotNull();
        assertThat(savedTask.getId()).isNotNull();
        assertThat(savedTask.getTitle()).isEqualTo("Test Task");
        assertThat(savedTask.getDescription()).isEqualTo("Test Description");
        assertThat(savedTask.getStatus()).isEqualTo(TaskStatus.TODO);
        assertThat(savedTask.getPriority()).isEqualTo(TaskPriority.MEDIUM);
        assertThat(savedTask.getUserId()).isEqualTo(1L);
        assertThat(savedTask.getCreatedBy()).isEqualTo(1L);
        assertThat(savedTask.getCreatedAt()).isNotNull();
        assertThat(savedTask.getDueDate()).isNotNull();
    }

    @Test
    void testFindByUserId() {
        Task task1 = createTask("Task 1", "Description 1", 1L);
        Task task2 = createTask("Task 2", "Description 2", 1L);
        Task task3 = createTask("Task 3", "Description 3", 2L);

        taskRepository.save(task1);
        taskRepository.save(task2);
        taskRepository.save(task3);

        List<Task> userTasks = taskRepository.findByUserId(1L);

        assertThat(userTasks).hasSize(2);
        assertThat(userTasks).extracting(Task::getTitle)
                .containsExactlyInAnyOrder("Task 1", "Task 2");
    }

    @Test
    void testFindAll() {
        Task task1 = createTask("Task 1", "Description 1", 1L);
        Task task2 = createTask("Task 2", "Description 2", 2L);
        Task task3 = createTask("Task 3", "Description 3", 3L);

        taskRepository.save(task1);
        taskRepository.save(task2);
        taskRepository.save(task3);

        List<Task> allTasks = taskRepository.findAll();

        assertThat(allTasks).hasSize(3);
        assertThat(allTasks).extracting(Task::getTitle)
                .containsExactlyInAnyOrder("Task 1", "Task 2", "Task 3");
    }

    @Test
    void testDeleteTask() {
        Task task = createTask("Test Task", "Test Description", 1L);
        Task savedTask = taskRepository.save(task);

        taskRepository.delete(savedTask);

        assertThat(taskRepository.findById(savedTask.getId())).isEmpty();
    }

    private Task createTask(String title, String description, Long userId) {
        Task task = new Task();
        task.setTitle(title);
        task.setDescription(description);
        task.setStatus(TaskStatus.TODO);
        task.setPriority(TaskPriority.MEDIUM);
        task.setUserId(userId);
        task.setCreatedBy(userId);
        task.setCreatedAt(LocalDateTime.now());
        task.setDueDate(LocalDateTime.now().plusDays(7));
        return task;
    }
} 