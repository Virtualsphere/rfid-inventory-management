package com.virtualsphere.rfidbackend.dto;

import com.virtualsphere.rfidbackend.model.Role;
import com.virtualsphere.rfidbackend.model.User;

import java.time.LocalDateTime;

public record UserResponse(Long id, String username, String fullName, Role role, String location,
                            boolean active, LocalDateTime createdAt) {
    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getUsername(), user.getFullName(),
                user.getRole(), user.getLocation(), user.isActive(), user.getCreatedAt());
    }
}
