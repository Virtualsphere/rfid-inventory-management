package com.virtualsphere.rfidbackend.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * Batch of scan events flushed from the mobile handheld app's offline queue
 * (or sent live, one event per scan) in a single call.
 */
@Data
public class InventorySyncRequest {
    @NotEmpty
    @Valid
    private List<SyncEventRequest> events;
}
