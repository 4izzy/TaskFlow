package com.taskflow.userservice.service;

import com.taskflow.userservice.dto.UserDTO;
import com.taskflow.userservice.entity.User;
import com.taskflow.userservice.entity.UserRole;
import com.taskflow.userservice.exception.ResourceAlreadyExistsException;
import com.taskflow.userservice.exception.ResourceNotFoundException;
import com.taskflow.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Transactional
    public UserDTO createUser(UserDTO userDTO) {
        log.info("Creating new user: {}", userDTO.getUsername());

        if (userRepository.existsByUsername(userDTO.getUsername())) {
            log.error("Username already exists: {}", userDTO.getUsername());
            throw new ResourceAlreadyExistsException("Username already exists");
        }

        if (userRepository.existsByEmail(userDTO.getEmail())) {
            log.error("Email already exists: {}", userDTO.getEmail());
            throw new ResourceAlreadyExistsException("Email already exists");
        }

        User user = new User();
        user.setUsername(userDTO.getUsername());
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        user.setEmail(userDTO.getEmail());
        user.setFirstName(userDTO.getFirstName());
        user.setLastName(userDTO.getLastName());
        user.setRole(userDTO.getRole());
        user.setEnabled(true);

        User savedUser = userRepository.save(user);
        log.info("User created successfully with id: {}", savedUser.getId());

        UserDTO createdUserDTO = convertToDTO(savedUser);
        kafkaTemplate.send("user-events", "USER_CREATED", createdUserDTO);
        return createdUserDTO;
    }

    @Transactional(readOnly = true)
    public UserDTO getUserById(Long id) {
        log.info("Getting user by id: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return convertToDTO(user);
    }

    @Transactional(readOnly = true)
    public List<UserDTO> getAllUsers() {
        log.info("Getting all users");
        return userRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public UserDTO updateUser(Long id, UserDTO userDTO) {
        log.info("Updating user with id: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        user.setEmail(userDTO.getEmail());
        user.setFirstName(userDTO.getFirstName());
        user.setLastName(userDTO.getLastName());
        user.setRole(userDTO.getRole());
        user.setEnabled(userDTO.isEnabled());

        User updatedUser = userRepository.save(user);
        log.info("User updated successfully");

        UserDTO updatedUserDTO = convertToDTO(updatedUser);
        kafkaTemplate.send("user-events", "USER_UPDATED", updatedUserDTO);
        return updatedUserDTO;
    }

    @Transactional
    public void deleteUser(Long id) {
        log.info("Deleting user with id: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        userRepository.delete(user);
        log.info("User deleted successfully");
        kafkaTemplate.send("user-events", "USER_DELETED", convertToDTO(user));
    }

    private UserDTO convertToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setRole(user.getRole());
        dto.setEnabled(user.isEnabled());
        return dto;
    }
} 