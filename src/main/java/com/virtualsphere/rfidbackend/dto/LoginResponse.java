package com.virtualsphere.rfidbackend.dto;

public record LoginResponse(String token, String username, String fullName, String role, String location) {
}
