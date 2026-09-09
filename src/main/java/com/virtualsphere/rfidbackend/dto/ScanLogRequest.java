package com.virtualsphere.rfidbackend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;

/** One ad hoc single-tag scan from the handheld's "Scan Inventory" live screen. */
@Data
public class ScanLogRequest {
    @NotBlank
    private String epc;

    /** Optional - defaults to the operator's own assigned location. */
    private String location;

    /** Optional - defaults to now. */
    private LocalDateTime scannedAt;
}
