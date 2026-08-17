package com.virtualsphere.rfidbackend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LocationRequest {
    @NotBlank
    private String name;

    private String address;
}
