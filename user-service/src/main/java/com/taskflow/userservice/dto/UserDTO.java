package com.taskflow.userservice.dto;

import com.taskflow.userservice.entity.UserRole;
import lombok.Data;

@Data
public class UserDTO {
    private Long id;
    private String username;
    private String password;
    private String email;
    private String firstName;
    private String lastName;
    private UserRole role;
    private boolean enabled;
} 