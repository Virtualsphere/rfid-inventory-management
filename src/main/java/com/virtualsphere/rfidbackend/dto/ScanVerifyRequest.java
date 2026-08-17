package com.virtualsphere.rfidbackend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * Sent by the mobile handheld app after a bulk scan pass over a location: the
 * location being verified, plus every EPC the handheld actually read. The
 * backend compares this against everything marked IN for that location.
 */
@Data
public class ScanVerifyRequest {
    @NotBlank
    private String location;

    @NotNull
    private List<String> scannedEpcs;
}
