package com.virtualsphere.rfidbackend.dto;

import com.virtualsphere.rfidbackend.model.InventoryStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * One scan event from the mobile handheld app - either read live or queued
 * locally while offline and flushed later via /api/inventory/sync. scannedAt
 * is the real time the operator scanned the tag, not when it reached the
 * server, so status history stays accurate regardless of sync delay.
 */
@Data
public class SyncEventRequest {
    @NotBlank
    private String epc;

    /** IN or OUT - the status the handheld scan should set the item to. */
    @NotNull
    private InventoryStatus status;

    /** Optional - defaults to the item's current location, or the requester's assigned location. */
    private String location;

    @NotNull
    private LocalDateTime scannedAt;

    private String notes;
}
