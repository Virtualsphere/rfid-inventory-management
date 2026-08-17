package com.virtualsphere.rfidbackend.dto;

import com.virtualsphere.rfidbackend.model.Location;

import java.time.LocalDateTime;

public record LocationResponse(Long id, String name, String address, LocalDateTime createdAt) {
    public static LocationResponse from(Location location) {
        return new LocationResponse(location.getId(), location.getName(), location.getAddress(), location.getCreatedAt());
    }
}
