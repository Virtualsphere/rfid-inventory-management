package com.virtualsphere.rfidbackend.dto;

import com.virtualsphere.rfidbackend.model.Role;

public record LoginResponse(String token, String username, String fullName, Role role, String location) {
}
