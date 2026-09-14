package com.virtualsphere.rfidbackend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/** One tag read from the admin desktop's security/checkpoint gate reader. */
@Data
public class CheckpointScanRequest {
    @NotBlank
    private String epc;
}
