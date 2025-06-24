package com.taskflow.userservice.service;

import com.taskflow.userservice.dto.UserDTO;
import com.taskflow.userservice.entity.User;
import com.taskflow.userservice.entity.UserRole;
import com.taskflow.userservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private UserDTO testUserDTO;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("password123");

        testUserDTO = new UserDTO();
        testUserDTO.setId(1L);
        testUserDTO.setUsername("testuser");
        testUserDTO.setEmail("test@example.com");
        testUserDTO.setPassword("password123");
    }

    @Test
    void testCreateUser() {
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        UserDTO result = userService.createUser(testUserDTO);

        assertNotNull(result);
        assertEquals(testUserDTO.getUsername(), result.getUsername());
        assertEquals(testUserDTO.getEmail(), result.getEmail());
        verify(userRepository).save(any(User.class));
        verify(kafkaTemplate).send(eq("user-events"), eq("USER_CREATED"), any());
    }

    @Test
    void testGetUserById() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        UserDTO result = userService.getUserById(1L);

        assertNotNull(result);
        assertEquals(testUser.getUsername(), result.getUsername());
        assertEquals(testUser.getEmail(), result.getEmail());
    }

    @Test
    void testGetAllUsers() {
        List<User> users = Arrays.asList(testUser);
        when(userRepository.findAll()).thenReturn(users);

        List<UserDTO> result = userService.getAllUsers();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testUser.getUsername(), result.get(0).getUsername());
    }

    @Test
    void testUpdateUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        UserDTO updatedDTO = new UserDTO();
        updatedDTO.setUsername("updateduser");
        updatedDTO.setEmail("updated@example.com");

        UserDTO result = userService.updateUser(1L, updatedDTO);

        assertNotNull(result);
        assertEquals(updatedDTO.getUsername(), result.getUsername());
        assertEquals(updatedDTO.getEmail(), result.getEmail());
        verify(userRepository).save(any(User.class));
        verify(kafkaTemplate).send(eq("user-events"), eq("USER_UPDATED"), any());
    }

    @Test
    void testDeleteUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        doNothing().when(userRepository).delete(any(User.class));

        userService.deleteUser(1L);

        verify(userRepository).delete(any(User.class));
        verify(kafkaTemplate).send(eq("user-events"), eq("USER_DELETED"), any());
    }

    @Test
    void testCreateUserWithExistingUsername() {
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(testUser));

        assertThrows(RuntimeException.class, () -> userService.createUser(testUserDTO));
    }

    @Test
    void testCreateUserWithExistingEmail() {
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));

        assertThrows(RuntimeException.class, () -> userService.createUser(testUserDTO));
    }
} 